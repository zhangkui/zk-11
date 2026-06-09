package com.charging.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 充电系统自定义配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "charging")
public class ChargingProperties {

    private Reservation reservation = new Reservation();
    private Queue queue = new Queue();
    private int timeSlots = 24;

    @Data
    public static class Reservation {
        private int timeOutMinutes = 15;
    }

    @Data
    public static class Queue {
        private int timeOutMinutes = 10;
    }
}
