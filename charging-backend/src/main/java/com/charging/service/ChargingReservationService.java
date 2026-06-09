package com.charging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.charging.dto.ReservationCancelDTO;
import com.charging.dto.ReservationCreateDTO;
import com.charging.entity.ChargingReservation;

public interface ChargingReservationService extends IService<ChargingReservation> {

    ChargingReservation create(ReservationCreateDTO dto);

    void cancel(ReservationCancelDTO dto);

    IPage<ChargingReservation> pageByUser(Long userId, Integer pageNum, Integer pageSize);

    void confirmArrive(Long id);

    void releaseTimeoutReservations();

    ChargingReservation getByNo(String reservationNo);
}
