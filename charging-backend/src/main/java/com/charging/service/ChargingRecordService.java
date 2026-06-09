package com.charging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.charging.dto.ChargingEndDTO;
import com.charging.dto.ChargingStartDTO;
import com.charging.entity.ChargingRecord;

public interface ChargingRecordService extends IService<ChargingRecord> {

    ChargingRecord startCharging(ChargingStartDTO dto);

    ChargingRecord endCharging(ChargingEndDTO dto);

    IPage<ChargingRecord> pageByUser(Long userId, Integer pageNum, Integer pageSize);

    ChargingRecord getCurrentCharging(Long userId, Long stationId);
}
