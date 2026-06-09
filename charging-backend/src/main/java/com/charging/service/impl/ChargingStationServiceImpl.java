package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.Constants;
import com.charging.dto.StationQueryDTO;
import com.charging.dto.StationSaveDTO;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingStation;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.service.ChargingStationService;
import com.charging.vo.StationDetailVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChargingStationServiceImpl extends ServiceImpl<ChargingStationMapper, ChargingStation> implements ChargingStationService {

    @Resource
    private ChargingPileMapper chargingPileMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public IPage<ChargingStation> pageList(StationQueryDTO dto) {
        LambdaQueryWrapper<ChargingStation> wrapper = new LambdaQueryWrapper<>();
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            wrapper.like(ChargingStation::getName, dto.getName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(ChargingStation::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(ChargingStation::getCreateTime);
        return this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
    }

    @Override
    public StationDetailVO getDetail(Long id) {
        ChargingStation station = this.getById(id);
        if (station == null) {
            throw new BusinessException("站点不存在");
        }

        StationDetailVO vo = new StationDetailVO();
        BeanUtils.copyProperties(station, vo);
        vo.setStatusName(station.getStatus() == 1 ? "启用" : "禁用");

        LambdaQueryWrapper<ChargingPile> pileWrapper = new LambdaQueryWrapper<>();
        pileWrapper.eq(ChargingPile::getStationId, id);
        pileWrapper.orderByAsc(ChargingPile::getPileNo);
        List<ChargingPile> piles = chargingPileMapper.selectList(pileWrapper);
        vo.setPiles(piles);

        String queueCountKey = Constants.REDIS_STATION_QUEUE_COUNT_KEY + id;
        Integer queueCount = (Integer) redisTemplate.opsForValue().get(queueCountKey);
        if (queueCount == null) {
            queueCount = 0;
        }
        vo.setQueueCount(queueCount);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStation(StationSaveDTO dto) {
        ChargingStation station;
        if (dto.getId() != null) {
            station = this.getById(dto.getId());
            if (station == null) {
                throw new BusinessException("站点不存在");
            }
        } else {
            station = new ChargingStation();
            station.setAvailablePiles(dto.getTotalPiles());
            station.setStatus(1);
        }

        BeanUtils.copyProperties(dto, station);
        if (dto.getStatus() == null && dto.getId() == null) {
            station.setStatus(1);
        }

        this.saveOrUpdate(station);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ChargingStation station = this.getById(id);
        if (station == null) {
            throw new BusinessException("站点不存在");
        }
        station.setStatus(status);
        this.updateById(station);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAvailablePiles(Long stationId) {
        ChargingStation station = this.getById(stationId);
        if (station == null) {
            throw new BusinessException("站点不存在");
        }

        LambdaQueryWrapper<ChargingPile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingPile::getStationId, stationId);
        wrapper.eq(ChargingPile::getStatus, Constants.PileStatus.IDLE);
        Long availableCount = chargingPileMapper.selectCount(wrapper);

        station.setAvailablePiles(availableCount.intValue());
        this.updateById(station);
    }
}
