package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.QueueJoinDTO;
import com.charging.entity.ChargingQueue;
import com.charging.vo.QueueStatusVO;

import java.util.List;

/**
 * 排队服务接口
 */
public interface ChargingQueueService {

    ChargingQueue join(QueueJoinDTO dto);

    QueueStatusVO getStatus(Long id);

    Page<QueueStatusVO> page(Long userId, Long stationId, Integer status, Integer pageNum, Integer pageSize);

    void callNext(Long stationId);

    void complete(Long id);

    void cancel(Long id);

    List<ChargingQueue> getTimeoutQueues();

    void releaseTimeoutQueue(ChargingQueue queue);

    int getAheadCount(Long stationId, Integer queueNumber);
}
