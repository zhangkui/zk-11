package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.ChargingQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChargingQueueMapper extends BaseMapper<ChargingQueue> {

    @Select("SELECT COALESCE(MAX(queue_number), 0) FROM charging_queue WHERE station_id = #{stationId} AND DATE(create_time) = DATE(#{date}) AND deleted = 0")
    Integer getMaxQueueNumber(@Param("stationId") Long stationId, @Param("date") LocalDateTime date);

    @Select("SELECT * FROM charging_queue WHERE station_id = #{stationId} AND status = 0 AND deleted = 0 ORDER BY queue_number ASC LIMIT 1")
    ChargingQueue getNextWaitingQueue(@Param("stationId") Long stationId);

    @Select("SELECT COUNT(*) FROM charging_queue WHERE station_id = #{stationId} AND status = 0 AND deleted = 0 AND queue_number < #{queueNumber}")
    Integer getAheadCount(@Param("stationId") Long stationId, @Param("queueNumber") Integer queueNumber);

    @Select("SELECT * FROM charging_queue WHERE status IN (0,1) AND expire_time < #{now} AND deleted = 0")
    List<ChargingQueue> findTimeoutQueues(@Param("now") LocalDateTime now);

    @Select("SELECT * FROM charging_queue WHERE user_id = #{userId} AND station_id = #{stationId} AND status IN (0,1) AND deleted = 0")
    ChargingQueue findActiveQueueByUserAndStation(@Param("userId") Long userId, @Param("stationId") Long stationId);
}
