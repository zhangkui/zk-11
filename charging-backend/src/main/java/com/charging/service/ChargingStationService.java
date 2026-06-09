package com.charging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.charging.dto.StationQueryDTO;
import com.charging.dto.StationSaveDTO;
import com.charging.entity.ChargingStation;
import com.charging.vo.StationDetailVO;

public interface ChargingStationService extends IService<ChargingStation> {

    IPage<ChargingStation> pageList(StationQueryDTO dto);

    StationDetailVO getDetail(Long id);

    void saveStation(StationSaveDTO dto);

    void updateStatus(Long id, Integer status);

    void updateAvailablePiles(Long stationId);
}
