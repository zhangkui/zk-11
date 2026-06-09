package com.charging.task;

import com.charging.entity.ChargingQueue;
import com.charging.entity.Reservation;
import com.charging.service.ChargingQueueService;
import com.charging.service.ReservationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 超时释放定时任务
 * 每分钟执行一次，处理超时的预约和排队叫号
 */
@Slf4j
@Component
public class TimeoutReleaseTask {

    @Resource
    private ReservationService reservationService;

    @Resource
    private ChargingQueueService chargingQueueService;

    /**
     * 每分钟执行，处理超时预约
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutReservations() {
        log.info("开始处理超时预约...");
        try {
            List<Reservation> timeoutReservations = reservationService.getTimeoutReservations();
            for (Reservation reservation : timeoutReservations) {
                try {
                    log.info("释放超时预约：reservationId={}, userId={}", reservation.getId(), reservation.getUserId());
                    reservationService.releaseTimeoutReservation(reservation);
                } catch (Exception e) {
                    log.error("释放超时预约失败：reservationId={}", reservation.getId(), e);
                }
            }
            log.info("处理超时预约完成，共处理{}条", timeoutReservations.size());
        } catch (Exception e) {
            log.error("处理超时预约异常", e);
        }
    }

    /**
     * 每分钟执行，处理超时叫号
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutQueues() {
        log.info("开始处理超时叫号...");
        try {
            List<ChargingQueue> timeoutQueues = chargingQueueService.getTimeoutQueues();
            for (ChargingQueue queue : timeoutQueues) {
                try {
                    log.info("释放超时叫号：queueId={}, userId={}", queue.getId(), queue.getUserId());
                    chargingQueueService.releaseTimeoutQueue(queue);
                } catch (Exception e) {
                    log.error("释放超时叫号失败：queueId={}", queue.getId(), e);
                }
            }
            log.info("处理超时叫号完成，共处理{}条", timeoutQueues.size());
        } catch (Exception e) {
            log.error("处理超时叫号异常", e);
        }
    }
}
