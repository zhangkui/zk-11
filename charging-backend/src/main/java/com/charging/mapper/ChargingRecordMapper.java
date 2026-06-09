package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.ChargingRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChargingRecordMapper extends BaseMapper<ChargingRecord> {
}
