package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.charging.common.Constants;
import com.charging.entity.*;
import com.charging.mapper.*;
import com.charging.service.DashboardService;
import com.charging.vo.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private ChargingStationMapper stationMapper;

    @Resource
    private ChargingPileMapper pileMapper;

    @Resource
    private ChargingQueueMapper queueMapper;

    @Resource
    private ChargingRecordMapper recordMapper;

    @Resource
    private ChargingFeeMapper feeMapper;

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO vo = new DashboardStatsVO();

        vo.setTotalStations(stationMapper.selectCount(null));
        vo.setTotalPiles(pileMapper.selectCount(null));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();

        LambdaQueryWrapper<ChargingQueue> queueWrapper = new LambdaQueryWrapper<>();
        queueWrapper.ge(ChargingQueue::getCreateTime, todayStart);
        queueWrapper.lt(ChargingQueue::getCreateTime, tomorrowStart);
        vo.setTodayQueue(queueMapper.selectCount(queueWrapper));

        LambdaQueryWrapper<ChargingFee> feeWrapper = new LambdaQueryWrapper<>();
        feeWrapper.ge(ChargingFee::getCreateTime, todayStart);
        feeWrapper.lt(ChargingFee::getCreateTime, tomorrowStart);
        feeWrapper.eq(ChargingFee::getPayStatus, Constants.PayStatus.PAID);
        List<ChargingFee> todayFees = feeMapper.selectList(feeWrapper);
        BigDecimal todayIncome = todayFees.stream()
                .map(ChargingFee::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodayIncome(todayIncome);

        vo.setPileStatusStats(getPileStatusStats());
        vo.setWeekTrend(getWeekTrend());
        vo.setStationStats(getStationStats(todayStart, tomorrowStart));

        return vo;
    }

    private List<PileStatusStatsVO> getPileStatusStats() {
        List<PileStatusStatsVO> list = new ArrayList<>();

        int[] statuses = {
                Constants.PileStatus.IDLE,
                Constants.PileStatus.IN_USE,
                Constants.PileStatus.RESERVED,
                Constants.PileStatus.FAULT,
                Constants.PileStatus.MAINTENANCE
        };
        String[] statusNames = {"空闲", "使用中", "预约中", "故障", "维护中"};

        for (int i = 0; i < statuses.length; i++) {
            PileStatusStatsVO vo = new PileStatusStatsVO();
            vo.setStatus(statuses[i]);
            vo.setStatusName(statusNames[i]);
            LambdaQueryWrapper<ChargingPile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChargingPile::getStatus, statuses[i]);
            vo.setCount(pileMapper.selectCount(wrapper));
            list.add(vo);
        }

        return list;
    }

    private List<TrendStatsVO> getWeekTrend() {
        List<TrendStatsVO> list = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            TrendStatsVO vo = new TrendStatsVO();
            vo.setDate(date.format(formatter));

            LambdaQueryWrapper<ChargingRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.ge(ChargingRecord::getCreateTime, start);
            recordWrapper.lt(ChargingRecord::getCreateTime, end);
            List<ChargingRecord> records = recordMapper.selectList(recordWrapper);
            BigDecimal totalKwh = records.stream()
                    .map(r -> r.getChargedKwh() != null ? r.getChargedKwh() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setTotalKwh(totalKwh);

            LambdaQueryWrapper<ChargingFee> feeWrapper = new LambdaQueryWrapper<>();
            feeWrapper.ge(ChargingFee::getCreateTime, start);
            feeWrapper.lt(ChargingFee::getCreateTime, end);
            feeWrapper.eq(ChargingFee::getPayStatus, Constants.PayStatus.PAID);
            List<ChargingFee> fees = feeMapper.selectList(feeWrapper);
            BigDecimal totalIncome = fees.stream()
                    .map(ChargingFee::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setTotalIncome(totalIncome);

            list.add(vo);
        }

        return list;
    }

    private List<StationStatsVO> getStationStats(LocalDateTime todayStart, LocalDateTime tomorrowStart) {
        List<StationStatsVO> list = new ArrayList<>();
        List<ChargingStation> stations = stationMapper.selectList(null);

        for (ChargingStation station : stations) {
            StationStatsVO vo = new StationStatsVO();
            vo.setId(station.getId());
            vo.setName(station.getName());
            vo.setTotalPiles(station.getTotalPiles());
            vo.setAvailablePiles(station.getAvailablePiles());

            LambdaQueryWrapper<ChargingRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.ge(ChargingRecord::getCreateTime, todayStart);
            recordWrapper.lt(ChargingRecord::getCreateTime, tomorrowStart);
            recordWrapper.eq(ChargingRecord::getStationId, station.getId());
            List<ChargingRecord> records = recordMapper.selectList(recordWrapper);
            BigDecimal totalKwh = records.stream()
                    .map(r -> r.getChargedKwh() != null ? r.getChargedKwh() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setTodayKwh(totalKwh);

            LambdaQueryWrapper<ChargingFee> feeWrapper = new LambdaQueryWrapper<>();
            feeWrapper.ge(ChargingFee::getCreateTime, todayStart);
            feeWrapper.lt(ChargingFee::getCreateTime, tomorrowStart);
            feeWrapper.eq(ChargingFee::getPayStatus, Constants.PayStatus.PAID);
            List<Long> recordIds = records.stream().map(ChargingRecord::getId).toList();
            if (!recordIds.isEmpty()) {
                feeWrapper.in(ChargingFee::getRecordId, recordIds);
                List<ChargingFee> fees = feeMapper.selectList(feeWrapper);
                BigDecimal totalIncome = fees.stream()
                        .map(ChargingFee::getPayAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setTodayIncome(totalIncome);
            } else {
                vo.setTodayIncome(BigDecimal.ZERO);
            }

            list.add(vo);
        }

        return list;
    }
}
