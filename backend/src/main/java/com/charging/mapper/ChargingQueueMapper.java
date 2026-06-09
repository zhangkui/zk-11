package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.ChargingQueue;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排队Mapper
 */
@Mapper
public interface ChargingQueueMapper extends BaseMapper<ChargingQueue> {
}
