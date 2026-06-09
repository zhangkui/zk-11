package com.charging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.charging.dto.QueueJoinDTO;
import com.charging.entity.ChargingQueue;
import com.charging.vo.QueueInfoVO;

public interface ChargingQueueService extends IService<ChargingQueue> {

    QueueInfoVO joinQueue(QueueJoinDTO dto);

    void cancelQueue(Long id);

    QueueInfoVO callNext(Long stationId);

    QueueInfoVO getQueueInfo(Long id);

    IPage<QueueInfoVO> pageByUser(Long userId, Integer pageNum, Integer pageSize);

    void releaseTimeoutQueues();

    Integer getAheadCount(Long stationId, Integer queueNumber);
}
