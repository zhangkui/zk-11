package com.charging.common;

public class Constants {

    public static final String REDIS_KEY_PREFIX = "charging:";

    public static final String REDIS_QUEUE_KEY = REDIS_KEY_PREFIX + "queue:";

    public static final String REDIS_RESERVATION_KEY = REDIS_KEY_PREFIX + "reservation:";

    public static final String REDIS_PILE_STATUS_KEY = REDIS_KEY_PREFIX + "pile:status:";

    public static final String REDIS_STATION_QUEUE_COUNT_KEY = REDIS_KEY_PREFIX + "station:queue:count:";

    public static final String REDIS_QUEUE_NUMBER_KEY = REDIS_KEY_PREFIX + "queue:number:";

    public static final String REDIS_USER_TOKEN_KEY = REDIS_KEY_PREFIX + "user:token:";

    public static final String REQUEST_HEADER_TOKEN = "Authorization";

    public static final String REQUEST_ATTR_USER_ID = "userId";

    public static final String REQUEST_ATTR_USER_ROLE = "userRole";

    public static interface PileStatus {
        int IDLE = 0;
        int IN_USE = 1;
        int RESERVED = 2;
        int FAULT = 3;
        int MAINTENANCE = 4;
    }

    public static interface PileType {
        int FAST = 1;
        int SLOW = 2;
    }

    public static interface ReservationStatus {
        int PENDING = 0;
        int CONFIRMED = 1;
        int IN_PROGRESS = 2;
        int COMPLETED = 3;
        int CANCELLED = 4;
        int TIMEOUT = 5;
    }

    public static interface QueueStatus {
        int WAITING = 0;
        int CALLED = 1;
        int USED = 2;
        int CANCELLED = 3;
        int TIMEOUT = 4;
    }

    public static interface ChargingStatus {
        int CHARGING = 0;
        int COMPLETED = 1;
        int ABNORMAL = 2;
    }

    public static interface PayStatus {
        int UNPAID = 0;
        int PAID = 1;
        int REFUNDED = 2;
    }

    public static interface PayMethod {
        int BALANCE = 1;
        int WECHAT = 2;
        int ALIPAY = 3;
    }

    public static interface UserRole {
        int USER = 1;
        int ADMIN = 2;
    }
}
