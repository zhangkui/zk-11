package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.ReservationCreateDTO;
import com.charging.entity.Reservation;
import com.charging.vo.ReservationVO;

import java.util.List;

/**
 * 预约服务接口
 */
public interface ReservationService {

    Reservation create(ReservationCreateDTO dto);

    ReservationVO getDetail(Long id);

    Page<ReservationVO> page(Long userId, Long stationId, Integer status, Integer pageNum, Integer pageSize);

    void cancel(Long id);

    void checkIn(Long id);

    void complete(Long id);

    List<Reservation> getTimeoutReservations();

    void releaseTimeoutReservation(Reservation reservation);
}
