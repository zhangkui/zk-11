package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.StationQueryDTO;
import com.charging.dto.StationSaveDTO;
import com.charging.entity.ChargingStation;
import com.charging.vo.StationDetailVO;
import com.charging.vo.TimeSlotStatusVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 充电站服务接口
 */
public interface ChargingStationService {

    Page<ChargingStation> page(StationQueryDTO queryDTO);

    StationDetailVO getDetail(Long id);

    void save(StationSaveDTO saveDTO);

    void updateStatus(Long id, Integer status);

    List<TimeSlotStatusVO> getTimeSlotStatus(Long stationId, LocalDate date);

    boolean checkTimeSlotAvailable(Long stationId, LocalDate date, Integer timeSlot);

    void decreaseAvailablePile(Long stationId);

    void increaseAvailablePile(Long stationId);
}
