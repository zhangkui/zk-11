package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.Constants;
import com.charging.dto.QueueJoinDTO;
import com.charging.entity.ChargingQueue;
import com.charging.entity.ChargingStation;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingQueueMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.service.ChargingQueueService;
import com.charging.vo.QueueInfoVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ChargingQueueServiceImpl extends ServiceImpl<ChargingQueueMapper, ChargingQueue> implements ChargingQueueService {

    @Resource
    private ChargingStationMapper chargingStationMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${charging.queue.timeout-minutes:15}")
    private Integer timeoutMinutes;

    @Value("${charging.queue.overtime-minutes:30}")
    private Integer overtimeMinutes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueueInfoVO joinQueue(QueueJoinDTO dto) {
        ChargingStation station = chargingStationMapper.selectById(dto.getStationId());
        if (station == null || station.getStatus() != 1) {
            throw new BusinessException("站点不存在或未启用");
        }

        ChargingQueue existingQueue = this.baseMapper.findActiveQueueByUserAndStation(dto.getUserId(), dto.getStationId());
        if (existingQueue != null) {
            throw new BusinessException("您已在该站点排队中");
        }

        Integer maxQueueNumber = this.baseMapper.getMaxQueueNumber(dto.getStationId(), LocalDateTime.now());
        int newQueueNumber = (maxQueueNumber == null ? 0 : maxQueueNumber) + 1;

        ChargingQueue queue = new ChargingQueue();
        queue.setQueueNo("QUE" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        queue.setUserId(dto.getUserId());
        queue.setStationId(dto.getStationId());
        queue.setQueueNumber(newQueueNumber);
        queue.setStatus(Constants.QueueStatus.WAITING);
        queue.setPileType(dto.getPileType());
        queue.setEstimatedWaitTime(newQueueNumber * 15);
        queue.setExpireTime(LocalDateTime.now().plusHours(24));
        queue.setRemark(dto.getRemark());
        this.save(queue);

        String queueKey = Constants.REDIS_QUEUE_KEY + dto.getStationId();
        redisTemplate.opsForList().rightPush(queueKey, queue.getId());
        redisTemplate.expire(queueKey, 24, TimeUnit.HOURS);

        String countKey = Constants.REDIS_STATION_QUEUE_COUNT_KEY + dto.getStationId();
        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, 24, TimeUnit.HOURS);

        return convertToVO(queue, station);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelQueue(Long id) {
        ChargingQueue queue = this.getById(id);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }

        if (queue.getStatus() != Constants.QueueStatus.WAITING && queue.getStatus() != Constants.QueueStatus.CALLED) {
            throw new BusinessException("当前状态不支持取消");
        }

        queue.setStatus(Constants.QueueStatus.CANCELLED);
        queue.setCancelTime(LocalDateTime.now());
        this.updateById(queue);

        String queueKey = Constants.REDIS_QUEUE_KEY + queue.getStationId();
        redisTemplate.opsForList().remove(queueKey, 1, queue.getId());

        String countKey = Constants.REDIS_STATION_QUEUE_COUNT_KEY + queue.getStationId();
        redisTemplate.opsForValue().decrement(countKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QueueInfoVO callNext(Long stationId) {
        ChargingStation station = chargingStationMapper.selectById(stationId);
        if (station == null) {
            throw new BusinessException("站点不存在");
        }

        String queueKey = Constants.REDIS_QUEUE_KEY + stationId;
        Object queueIdObj = redisTemplate.opsForList().leftPop(queueKey);

        ChargingQueue queue = null;
        if (queueIdObj != null) {
            Long queueId = Long.valueOf(queueIdObj.toString());
            queue = this.getById(queueId);
        }

        if (queue == null) {
            queue = this.baseMapper.getNextWaitingQueue(stationId);
        }

        if (queue == null) {
            throw new BusinessException("当前没有等待中的用户");
        }

        queue.setStatus(Constants.QueueStatus.CALLED);
        queue.setCalledTime(LocalDateTime.now());
        queue.setExpireTime(LocalDateTime.now().plusMinutes(timeoutMinutes));
        this.updateById(queue);

        String countKey = Constants.REDIS_STATION_QUEUE_COUNT_KEY + stationId;
        redisTemplate.opsForValue().decrement(countKey);

        return convertToVO(queue, station);
    }

    @Override
    public QueueInfoVO getQueueInfo(Long id) {
        ChargingQueue queue = this.getById(id);
        if (queue == null) {
            throw new BusinessException("排队记录不存在");
        }

        ChargingStation station = chargingStationMapper.selectById(queue.getStationId());
        return convertToVO(queue, station);
    }

    @Override
    public IPage<QueueInfoVO> pageByUser(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ChargingQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingQueue::getUserId, userId);
        wrapper.orderByDesc(ChargingQueue::getCreateTime);
        IPage<ChargingQueue> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        return page.convert(queue -> {
            ChargingStation station = chargingStationMapper.selectById(queue.getStationId());
            return convertToVO(queue, station);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseTimeoutQueues() {
        List<ChargingQueue> timeoutQueues = this.baseMapper.findTimeoutQueues(LocalDateTime.now());

        for (ChargingQueue queue : timeoutQueues) {
            queue.setStatus(Constants.QueueStatus.TIMEOUT);
            this.updateById(queue);

            String queueKey = Constants.REDIS_QUEUE_KEY + queue.getStationId();
            redisTemplate.opsForList().remove(queueKey, 1, queue.getId());
        }
    }

    @Override
    public Integer getAheadCount(Long stationId, Integer queueNumber) {
        return this.baseMapper.getAheadCount(stationId, queueNumber);
    }

    private QueueInfoVO convertToVO(ChargingQueue queue, ChargingStation station) {
        QueueInfoVO vo = new QueueInfoVO();
        vo.setId(queue.getId());
        vo.setQueueNo(queue.getQueueNo());
        vo.setQueueNumber(queue.getQueueNumber());
        vo.setStatus(queue.getStatus());
        vo.setStationId(queue.getStationId());
        vo.setPileType(queue.getPileType());
        vo.setEstimatedWaitTime(queue.getEstimatedWaitTime());
        vo.setCalledTime(queue.getCalledTime());
        vo.setExpireTime(queue.getExpireTime());
        vo.setCreateTime(queue.getCreateTime());

        if (station != null) {
            vo.setStationName(station.getName());
        }

        switch (queue.getStatus()) {
            case Constants.QueueStatus.WAITING:
                vo.setStatusName("排队中");
                break;
            case Constants.QueueStatus.CALLED:
                vo.setStatusName("叫号中");
                break;
            case Constants.QueueStatus.USED:
                vo.setStatusName("已使用");
                break;
            case Constants.QueueStatus.CANCELLED:
                vo.setStatusName("已取消");
                break;
            case Constants.QueueStatus.TIMEOUT:
                vo.setStatusName("已超时");
                break;
            default:
                vo.setStatusName("未知");
        }

        vo.setPileTypeName(queue.getPileType() != null && queue.getPileType() == 1 ? "快充" : "慢充");

        if (queue.getStatus() == Constants.QueueStatus.WAITING) {
            vo.setAheadCount(getAheadCount(queue.getStationId(), queue.getQueueNumber()));
        } else {
            vo.setAheadCount(0);
        }

        return vo;
    }
}
