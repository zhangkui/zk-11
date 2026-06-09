package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.ChargingEndDTO;
import com.charging.dto.ChargingStartDTO;
import com.charging.entity.ChargingRecord;
import com.charging.vo.ChargingRecordVO;

/**
 * 充电记录服务接口
 */
public interface ChargingRecordService {

    ChargingRecord start(ChargingStartDTO dto);

    ChargingRecord end(ChargingEndDTO dto);

    ChargingRecordVO getDetail(Long id);

    Page<ChargingRecordVO> page(Long userId, Long stationId, Integer status, Integer paymentStatus, Integer pageNum, Integer pageSize);

    void updatePaymentStatus(Long id, Integer paymentStatus, Integer paymentMethod, java.math.BigDecimal paidAmount);
}
