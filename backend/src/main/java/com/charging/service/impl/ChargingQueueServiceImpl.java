package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.ResultCode;
import com.charging.config.ChargingProperties;
import com.charging.dto.QueueJoinDTO;
import com.charging.entity.ChargingQueue;
import com.charging.entity.ChargingStation;
import com.charging.entity.UserInfo;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.ChargingQueueMapper;
import com.charging.mapper.UserInfoMapper;
import com.charging.service.ChargingQueueService;
import com.charging.util.RedisKeyUtil;
import com.charging.vo.QueueStatusVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 排队服务实现类
 */
@Service
public class ChargingQueueServiceImpl extends ServiceImpl<ChargingQueueMapper, ChargingQueue> implements ChargingQueueService {

    @Resource
    private ChargingStationMapper chargingStationMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ChargingProperties chargingProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChargingQueue join(QueueJoinDTO dto) {
        UserInfo user = userInfoMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        ChargingStation station = chargingStationMapper.selectById(dto.getStationId());
        if (station == null) {
            throw new BusinessException(ResultCode.STATION_NOT_FOUND);
        }
        if (station.getStatus() != 1) {
            throw new BusinessException(ResultCode.STATION_CLOSED);
        }

        LambdaQueryWrapper<ChargingQueue> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(ChargingQueue::getUserId, dto.getUserId());
        existingWrapper.eq(ChargingQueue::getStationId, dto.getStationId());
        existingWrapper.in(ChargingQueue::getStatus, 0, 1);
        Long existingCount = baseMapper.selectCount(existingWrapper);
        if (existingCount != null && existingCount > 0) {
            throw new BusinessException("您已在此站点排队中");
        }

        String queueKey = RedisKeyUtil.getQueueNumberKey(dto.getStationId());
        Long queueNumber = redisTemplate.opsForValue().increment(queueKey);
        if (queueNumber == null) {
            queueNumber = 1L;
            redisTemplate.opsForValue().set(queueKey, 1);
        }

        ChargingQueue queue = new ChargingQueue();
        queue.setStationId(dto.getStationId());
        queue.setUserId(dto.getUserId());
        queue.setCarPlate(dto.getCarPlate());
        queue.setQueueNumber(queueNumber.intValue());
        queue.setStatus(0);

        save(queue);

        return queue;
    }

    @Override
    public QueueStatusVO getStatus(Long id) {
        ChargingQueue queue = getById(id);
        if (queue == null) {
            throw new BusinessException(ResultCode.QUEUE_NOT_FOUND);
        }
        return convertToVO(queue);
    }

    @Override
    public Page<QueueStatusVO> page(Long userId, Long stationId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ChargingQueue> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(ChargingQueue::getUserId, userId);
        }
        if (stationId != null) {
            wrapper.eq(ChargingQueue::getStationId, stationId);
        }
        if (status != null) {
            wrapper.eq(ChargingQueue::getStatus, status);
        }
        wrapper.orderByAsc(ChargingQueue::getQueueNumber);

        Page<ChargingQueue> page = page(new Page<>(pageNum, pageSize), wrapper);

        Page<QueueStatusVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<QueueStatusVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callNext(Long stationId) {
        ChargingStation station = chargingStationMapper.selectById(stationId);
        if (station == null) {
            throw new BusinessException(ResultCode.STATION_NOT_FOUND);
        }

        String currentKey = RedisKeyUtil.getQueueCurrentKey(stationId);
        Integer currentNumber = (Integer) redisTemplate.opsForValue().get(currentKey);
        if (currentNumber == null) {
            currentNumber = 0;
        }

        LambdaQueryWrapper<ChargingQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingQueue::getStationId, stationId);
        wrapper.eq(ChargingQueue::getStatus, 0);
        wrapper.gt(ChargingQueue::getQueueNumber, currentNumber);
        wrapper.orderByAsc(ChargingQueue::getQueueNumber);
        wrapper.last("LIMIT 1");

        ChargingQueue nextQueue = getOne(wrapper);
        if (nextQueue == null) {
            throw new BusinessException("没有等待排队的用户");
        }

        nextQueue.setStatus(1);
        nextQueue.setCalledTime(LocalDateTime.now());
        nextQueue.setExpireTime(LocalDateTime.now().plusMinutes(chargingProperties.getQueue().getTimeOutMinutes()));

        updateById(nextQueue);

        redisTemplate.opsForValue().set(currentKey, nextQueue.getQueueNumber());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        ChargingQueue queue = getById(id);
        if (queue == null) {
            throw new BusinessException(ResultCode.QUEUE_NOT_FOUND);
        }
        if (queue.getStatus() == 2) {
            throw new BusinessException(ResultCode.QUEUE_COMPLETED);
        }

        queue.setStatus(2);
        updateById(queue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        ChargingQueue queue = getById(id);
        if (queue == null) {
            throw new BusinessException(ResultCode.QUEUE_NOT_FOUND);
        }
        if (queue.getStatus() == 2) {
            throw new BusinessException(ResultCode.QUEUE_COMPLETED);
        }

        queue.setStatus(3);
        updateById(queue);
    }

    @Override
    public List<ChargingQueue> getTimeoutQueues() {
        LambdaQueryWrapper<ChargingQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ChargingQueue::getStatus, 1);
        wrapper.isNotNull(ChargingQueue::getExpireTime);
        wrapper.lt(ChargingQueue::getExpireTime, LocalDateTime.now());
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseTimeoutQueue(ChargingQueue queue) {
        queue.setStatus(3);
        updateById(queue);
    }

    @Override
    public int getAheadCount(Long stationId, Integer queueNumber) {
        String currentKey = RedisKeyUtil.getQueueCurrentKey(stationId);
        Integer currentNumber = (Integer) redisTemplate.opsForValue().get(currentKey);
        if (currentNumber == null) {
            currentNumber = 0;
        }

        LambdaQueryWrapper<ChargingQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingQueue::getStationId, stationId);
        wrapper.eq(ChargingQueue::getStatus, 0);
        wrapper.gt(ChargingQueue::getQueueNumber, currentNumber);
        wrapper.lt(ChargingQueue::getQueueNumber, queueNumber);
        Long count = baseMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    private QueueStatusVO convertToVO(ChargingQueue queue) {
        QueueStatusVO vo = new QueueStatusVO();
        vo.setId(queue.getId());
        vo.setStationId(queue.getStationId());
        vo.setUserId(queue.getUserId());
        vo.setCarPlate(queue.getCarPlate());
        vo.setQueueNumber(queue.getQueueNumber());
        vo.setStatus(queue.getStatus());
        vo.setStatusDesc(getStatusDesc(queue.getStatus()));
        vo.setCalledTime(queue.getCalledTime());
        vo.setExpireTime(queue.getExpireTime());
        vo.setCreateTime(queue.getCreateTime());

        ChargingStation station = chargingStationMapper.selectById(queue.getStationId());
        if (station != null) {
            vo.setStationName(station.getName());
        }

        if (queue.getStatus() == 0) {
            vo.setAheadCount(getAheadCount(queue.getStationId(), queue.getQueueNumber()));
        } else {
            vo.setAheadCount(0);
        }

        return vo;
    }

    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "排队中";
            case 1 -> "已叫号";
            case 2 -> "已完成";
            case 3 -> "已过号";
            default -> "未知";
        };
    }
}
