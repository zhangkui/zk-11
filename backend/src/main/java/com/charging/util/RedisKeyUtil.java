package com.charging.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Redis Key 生成工具
 */
public class RedisKeyUtil {

    private static final String PREFIX = "charging:";
    private static final String TIME_SLOT_KEY = PREFIX + "time_slot:";
    private static final String QUEUE_NUMBER_KEY = PREFIX + "queue_number:";
    private static final String QUEUE_CURRENT_KEY = PREFIX + "queue_current:";
    private static final String STATION_AVAILABLE_KEY = PREFIX + "station_available:";
    private static final String LOCK_KEY = PREFIX + "lock:";

    /**
     * 生成时段状态Key
     */
    public static String getTimeSlotKey(Long stationId, LocalDate date, Integer timeSlot) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return TIME_SLOT_KEY + stationId + ":" + dateStr + ":" + timeSlot;
    }

    /**
     * 生成时段状态前缀Key（用于批量查询）
     */
    public static String getTimeSlotPrefix(Long stationId, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return TIME_SLOT_KEY + stationId + ":" + dateStr + ":*";
    }

    /**
     * 生成排队号自增Key
     */
    public static String getQueueNumberKey(Long stationId) {
        return QUEUE_NUMBER_KEY + stationId;
    }

    /**
     * 生成当前叫号Key
     */
    public static String getQueueCurrentKey(Long stationId) {
        return QUEUE_CURRENT_KEY + stationId;
    }

    /**
     * 生成站点可用充电桩Key
     */
    public static String getStationAvailableKey(Long stationId) {
        return STATION_AVAILABLE_KEY + stationId;
    }

    /**
     * 生成分布式锁Key
     */
    public static String getLockKey(String resource) {
        return LOCK_KEY + resource;
    }
}
