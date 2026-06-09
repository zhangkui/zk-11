CREATE DATABASE IF NOT EXISTS charging_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE charging_system;

CREATE TABLE IF NOT EXISTS charging_station (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '站点名称',
    address VARCHAR(255) NOT NULL COMMENT '站点地址',
    total_piles INT NOT NULL DEFAULT 0 COMMENT '充电桩总数',
    available_piles INT NOT NULL DEFAULT 0 COMMENT '可用充电桩数',
    price_per_kwh DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '每度电价格',
    service_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务费',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-关闭 1-开放',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电站点表';

CREATE TABLE IF NOT EXISTS charging_pile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    pile_number VARCHAR(50) NOT NULL COMMENT '充电桩编号',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '类型 1-快充 2-慢充',
    power INT NOT NULL COMMENT '功率(KW)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-空闲 1-使用中 2-预约中 3-故障',
    current_order_id BIGINT COMMENT '当前充电订单ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_station (station_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_pile_number (pile_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电桩表';

CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    car_plate VARCHAR(20) COMMENT '车牌号',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_phone (phone),
    INDEX idx_car_plate (car_plate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    pile_id BIGINT COMMENT '充电桩ID',
    reservation_date DATE NOT NULL COMMENT '预约日期',
    time_slot TINYINT NOT NULL COMMENT '预约时段 1-24',
    car_plate VARCHAR(20) NOT NULL COMMENT '车牌号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待确认 1-已确认 2-已取消 3-已超时 4-已完成',
    queue_number INT COMMENT '排队号',
    expire_time DATETIME COMMENT '过期时间',
    check_in_time DATETIME COMMENT '签到时间',
    cancel_time DATETIME COMMENT '取消时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id),
    INDEX idx_station (station_id),
    INDEX idx_status (status),
    INDEX idx_date_slot (reservation_date, time_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

CREATE TABLE IF NOT EXISTS charging_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    car_plate VARCHAR(20) NOT NULL COMMENT '车牌号',
    queue_number INT NOT NULL COMMENT '排队号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-排队中 1-已叫号 2-已完成 3-已过号',
    called_time DATETIME COMMENT '叫号时间',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_station_status (station_id, status),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排队表';

CREATE TABLE IF NOT EXISTS charging_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    pile_id BIGINT NOT NULL COMMENT '充电桩ID',
    reservation_id BIGINT COMMENT '预约ID',
    car_plate VARCHAR(20) NOT NULL COMMENT '车牌号',
    start_time DATETIME NOT NULL COMMENT '开始充电时间',
    end_time DATETIME COMMENT '结束充电时间',
    duration INT COMMENT '充电时长(分钟)',
    start_kwh DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '起始电量(kWh)',
    end_kwh DECIMAL(10,2) COMMENT '结束电量(kWh)',
    charged_kwh DECIMAL(10,2) COMMENT '充电量(kWh)',
    price_per_kwh DECIMAL(10,2) NOT NULL COMMENT '电费单价',
    service_fee DECIMAL(10,2) NOT NULL COMMENT '服务费',
    total_amount DECIMAL(10,2) COMMENT '总费用',
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    payment_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态 0-未支付 1-已支付 2-已退款',
    payment_time DATETIME COMMENT '支付时间',
    payment_method TINYINT COMMENT '支付方式 1-余额 2-微信 3-支付宝',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-充电中 1-已完成 2-异常结束',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id),
    INDEX idx_station (station_id),
    INDEX idx_pile (pile_id),
    INDEX idx_status (status),
    INDEX idx_payment (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电记录表';

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    charging_record_id BIGINT NOT NULL COMMENT '充电记录ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    payment_method TINYINT NOT NULL COMMENT '支付方式 1-余额 2-微信 3-支付宝',
    payment_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态 0-待支付 1-支付成功 2-支付失败 3-已退款',
    transaction_id VARCHAR(100) COMMENT '第三方交易号',
    pay_time DATETIME COMMENT '支付时间',
    refund_time DATETIME COMMENT '退款时间',
    refund_amount DECIMAL(10,2) COMMENT '退款金额',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user (user_id),
    INDEX idx_charging_record (charging_record_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

INSERT INTO user_info (phone, nickname, car_plate, balance) VALUES
('13800138001', '张三', '京A12345', 500.00),
('13800138002', '李四', '京B67890', 300.00),
('13800138003', '王五', '京C11111', 1000.00);

INSERT INTO charging_station (name, address, total_piles, available_piles, price_per_kwh, service_fee, status, longitude, latitude) VALUES
('中关村充电站', '北京市海淀区中关村大街1号', 10, 10, 1.20, 0.50, 1, 116.3083, 39.9816),
('国贸充电站', '北京市朝阳区国贸中心', 8, 8, 1.50, 0.60, 1, 116.4600, 39.9088),
('望京充电站', '北京市朝阳区望京SOHO', 12, 12, 1.30, 0.50, 1, 116.4700, 39.9900);

INSERT INTO charging_pile (station_id, pile_number, type, power, status) VALUES
(1, 'ZGC-001', 1, 120, 0),
(1, 'ZGC-002', 1, 120, 0),
(1, 'ZGC-003', 1, 120, 0),
(1, 'ZGC-004', 1, 120, 0),
(1, 'ZGC-005', 1, 120, 0),
(1, 'ZGC-006', 2, 60, 0),
(1, 'ZGC-007', 2, 60, 0),
(1, 'ZGC-008', 2, 60, 0),
(1, 'ZGC-009', 2, 60, 0),
(1, 'ZGC-010', 2, 60, 0),
(2, 'GM-001', 1, 180, 0),
(2, 'GM-002', 1, 180, 0),
(2, 'GM-003', 1, 180, 0),
(2, 'GM-004', 1, 180, 0),
(2, 'GM-005', 2, 90, 0),
(2, 'GM-006', 2, 90, 0),
(2, 'GM-007', 2, 90, 0),
(2, 'GM-008', 2, 90, 0),
(3, 'WJ-001', 1, 150, 0),
(3, 'WJ-002', 1, 150, 0),
(3, 'WJ-003', 1, 150, 0),
(3, 'WJ-004', 1, 150, 0),
(3, 'WJ-005', 1, 150, 0),
(3, 'WJ-006', 1, 150, 0),
(3, 'WJ-007', 2, 75, 0),
(3, 'WJ-008', 2, 75, 0),
(3, 'WJ-009', 2, 75, 0),
(3, 'WJ-010', 2, 75, 0),
(3, 'WJ-011', 2, 75, 0),
(3, 'WJ-012', 2, 75, 0);
