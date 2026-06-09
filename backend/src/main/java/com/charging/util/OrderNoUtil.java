package com.charging.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Date;

/**
 * 订单号生成工具
 */
public class OrderNoUtil {

    private static final String PREFIX_PAY = "PAY";
    private static final String PREFIX_CHARGING = "CRG";
    private static final String PREFIX_RESERVATION = "RSV";

    /**
     * 生成支付订单号
     */
    public static String generatePayOrderNo() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String randomStr = RandomUtil.randomNumbers(6);
        return PREFIX_PAY + dateStr + randomStr;
    }

    /**
     * 生成充电订单号
     */
    public static String generateChargingOrderNo() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String randomStr = RandomUtil.randomNumbers(6);
        return PREFIX_CHARGING + dateStr + randomStr;
    }

    /**
     * 生成预约订单号
     */
    public static String generateReservationOrderNo() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String randomStr = RandomUtil.randomNumbers(6);
        return PREFIX_RESERVATION + dateStr + randomStr;
    }
}
