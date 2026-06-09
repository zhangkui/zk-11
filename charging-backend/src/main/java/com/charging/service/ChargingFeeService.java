package com.charging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.charging.dto.FeePayDTO;
import com.charging.entity.ChargingFee;
import com.charging.entity.ChargingRecord;

public interface ChargingFeeService extends IService<ChargingFee> {

    ChargingFee createFee(ChargingRecord record);

    ChargingFee pay(FeePayDTO dto);

    IPage<ChargingFee> pageByUser(Long userId, Integer pageNum, Integer pageSize);

    ChargingFee getByRecordId(Long recordId);
}
