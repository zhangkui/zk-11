package com.charging.task;

import com.charging.service.ChargingQueueService;
import com.charging.service.ChargingReservationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TimeoutReleaseTask {

    @Resource
    private ChargingReservationService reservationService;

    @Resource
    private ChargingQueueService queueService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void releaseTimeoutReservation() {
        log.info("开始执行超时预约释放任务");
        try {
            reservationService.releaseTimeoutReservations();
            log.info("超时预约释放任务执行完成");
        } catch (Exception e) {
            log.error("超时预约释放任务执行异常", e);
        }
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void releaseTimeoutQueue() {
        log.info("开始执行超时排队释放任务");
        try {
            queueService.releaseTimeoutQueues();
            log.info("超时排队释放任务执行完成");
        } catch (Exception e) {
            log.error("超时排队释放任务执行异常", e);
        }
    }
}
