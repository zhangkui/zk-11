package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.ChargingReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChargingReservationMapper extends BaseMapper<ChargingReservation> {

    @Select("SELECT * FROM charging_reservation WHERE station_id = #{stationId} AND status IN (0,1,2) AND deleted = 0 " +
            "AND ((reserve_start_time <= #{endTime} AND reserve_end_time >= #{startTime})) " +
            "FOR UPDATE")
    List<ChargingReservation> findConflictingReservations(@Param("stationId") Long stationId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM charging_reservation WHERE station_id = #{stationId} AND pile_id = #{pileId} AND status IN (0,1,2) AND deleted = 0 " +
            "AND ((reserve_start_time <= #{endTime} AND reserve_end_time >= #{startTime})) " +
            "AND id != #{excludeId} " +
            "FOR UPDATE")
    List<ChargingReservation> findConflictingReservationsExcludeId(@Param("stationId") Long stationId,
                                                                   @Param("pileId") Long pileId,
                                                                   @Param("startTime") LocalDateTime startTime,
                                                                   @Param("endTime") LocalDateTime endTime,
                                                                   @Param("excludeId") Long excludeId);

    @Select("SELECT * FROM charging_reservation WHERE status IN (0,1) AND reserve_start_time < #{timeoutTime} AND deleted = 0")
    List<ChargingReservation> findTimeoutReservations(@Param("timeoutTime") LocalDateTime timeoutTime);
}
