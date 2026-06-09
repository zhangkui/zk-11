package com.charging.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    STATION_NOT_FOUND(1001, "充电站不存在"),
    STATION_CLOSED(1002, "充电站已关闭"),
    PILE_NOT_FOUND(1003, "充电桩不存在"),
    PILE_NOT_AVAILABLE(1004, "充电桩不可用"),

    USER_NOT_FOUND(2001, "用户不存在"),
    USER_DISABLED(2002, "用户已禁用"),
    BALANCE_NOT_ENOUGH(2003, "余额不足"),

    TIME_SLOT_OCCUPIED(3001, "该时段已被预约"),
    RESERVATION_NOT_FOUND(3002, "预约不存在"),
    RESERVATION_TIMEOUT(3003, "预约已超时"),
    RESERVATION_CANCELLED(3004, "预约已取消"),
    RESERVATION_COMPLETED(3005, "预约已完成"),

    QUEUE_NOT_FOUND(4001, "排队记录不存在"),
    QUEUE_CALLED(4002, "已叫号，请在规定时间内到达"),
    QUEUE_EXPIRED(4003, "叫号已超时"),
    QUEUE_COMPLETED(4004, "排队已完成"),

    CHARGING_NOT_FOUND(5001, "充电记录不存在"),
    CHARGING_IN_PROGRESS(5002, "充电进行中"),
    CHARGING_COMPLETED(5003, "充电已完成"),

    PAYMENT_NOT_FOUND(6001, "支付记录不存在"),
    PAYMENT_SUCCESS(6002, "支付成功"),
    PAYMENT_FAIL(6003, "支付失败");

    private final Integer code;
    private final String message;
}
