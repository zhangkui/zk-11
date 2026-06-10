CREATE DATABASE IF NOT EXISTS charging_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE charging_db;

DROP TABLE IF EXISTS charging_fee;
DROP TABLE IF EXISTS charging_record;
DROP TABLE IF EXISTS charging_queue;
DROP TABLE IF EXISTS charging_reservation;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS charging_pile;
DROP TABLE IF EXISTS charging_station;

CREATE TABLE charging_station (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '站点名称',
    address VARCHAR(255) NOT NULL COMMENT '站点地址',
    longitude DECIMAL(10,7) COMMENT '经度',
    latitude DECIMAL(10,7) COMMENT '纬度',
    total_piles INT NOT NULL DEFAULT 0 COMMENT '充电桩总数',
    available_piles INT NOT NULL DEFAULT 0 COMMENT '可用充电桩数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    opening_hours VARCHAR(50) COMMENT '营业时间',
    phone VARCHAR(20) COMMENT '联系电话',
    description TEXT COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_status (status),
    INDEX idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电站点表';

CREATE TABLE charging_pile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    station_id BIGINT NOT NULL COMMENT '所属站点ID',
    pile_no VARCHAR(50) NOT NULL COMMENT '充电桩编号',
    pile_type TINYINT NOT NULL COMMENT '类型：1-快充，2-慢充',
    power_rating DECIMAL(5,2) NOT NULL COMMENT '额定功率（kW）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-空闲，1-使用中，2-预约中，3-故障，4-维护中',
    current_order_no VARCHAR(50) COMMENT '当前订单号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_station_id (station_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_pile_no (pile_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电桩表';

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    license_plate VARCHAR(20) COMMENT '车牌号',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_phone (phone),
    INDEX idx_license_plate (license_plate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE charging_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    reservation_no VARCHAR(50) NOT NULL COMMENT '预约单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    pile_id BIGINT COMMENT '充电桩ID',
    reserve_start_time DATETIME NOT NULL COMMENT '预约开始时间',
    reserve_end_time DATETIME NOT NULL COMMENT '预约结束时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待确认，1-已确认，2-进行中，3-已完成，4-已取消，5-已超时',
    queue_number INT COMMENT '排队号',
    arrive_time DATETIME COMMENT '到店时间',
    cancel_time DATETIME COMMENT '取消时间',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_station_id (station_id),
    INDEX idx_status (status),
    INDEX idx_reserve_time (reserve_start_time, reserve_end_time),
    UNIQUE KEY uk_reservation_no (reservation_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约记录表';

CREATE TABLE charging_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    queue_no VARCHAR(50) NOT NULL COMMENT '排队单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    queue_number INT NOT NULL COMMENT '排队号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-排队中，1-叫号中，2-已使用，3-已取消，4-已超时',
    pile_type TINYINT COMMENT '期望充电桩类型：1-快充，2-慢充',
    estimated_wait_time INT COMMENT '预计等待时间（分钟）',
    called_time DATETIME COMMENT '叫号时间',
    cancel_time DATETIME COMMENT '取消时间',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_station_id (station_id),
    INDEX idx_status (status),
    INDEX idx_queue_number (station_id, queue_number),
    UNIQUE KEY uk_queue_no (queue_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排队记录表';

CREATE TABLE charging_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    record_no VARCHAR(50) NOT NULL COMMENT '充电记录编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '站点ID',
    pile_id BIGINT NOT NULL COMMENT '充电桩ID',
    reservation_id BIGINT COMMENT '关联预约ID',
    start_time DATETIME NOT NULL COMMENT '开始充电时间',
    end_time DATETIME COMMENT '结束充电时间',
    start_soc DECIMAL(5,2) COMMENT '开始SOC（%）',
    end_soc DECIMAL(5,2) COMMENT '结束SOC（%）',
    charged_kwh DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '充电量（kWh）',
    duration INT COMMENT '充电时长（分钟）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-充电中，1-已完成，2-异常终止',
    stop_reason VARCHAR(255) COMMENT '停止原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_station_id (station_id),
    INDEX idx_pile_id (pile_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    UNIQUE KEY uk_record_no (record_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电记录表';

CREATE TABLE charging_fee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    fee_no VARCHAR(50) NOT NULL COMMENT '费用单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_id BIGINT NOT NULL COMMENT '充电记录ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额（元）',
    electricity_fee DECIMAL(10,2) NOT NULL COMMENT '电费（元）',
    service_fee DECIMAL(10,2) NOT NULL COMMENT '服务费（元）',
    penalty_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '违约金（元）',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（元）',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额（元）',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-已退款',
    pay_time DATETIME COMMENT '支付时间',
    pay_method TINYINT COMMENT '支付方式：1-余额，2-微信，3-支付宝',
    price_per_kwh DECIMAL(5,2) NOT NULL COMMENT '电价（元/kWh）',
    service_fee_per_kwh DECIMAL(5,2) NOT NULL COMMENT '服务费单价（元/kWh）',
    charged_kwh DECIMAL(10,2) NOT NULL COMMENT '充电量（kWh）',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_record_id (record_id),
    INDEX idx_pay_status (pay_status),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_fee_no (fee_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用记录表';

INSERT INTO charging_station (name, address, longitude, latitude, total_piles, available_piles, status, opening_hours, phone, description) VALUES
('朝阳公园充电站', '北京市朝阳区朝阳公园西门', 116.472873, 39.933935, 8, 8, 1, '00:00-24:00', '400-800-1001', '位于朝阳公园西门停车场，提供快充和慢充服务'),
('国贸中心充电站', '北京市朝阳区国贸中心地下停车场', 116.460948, 39.915485, 12, 12, 1, '06:00-24:00', '400-800-1002', 'CBD核心区域，支持快速充电'),
('中关村充电站', '北京市海淀区中关村大街1号', 116.317665, 39.983566, 10, 10, 1, '00:00-24:00', '400-800-1003', '科技园区内，24小时服务'),
('望京SOHO充电站', '北京市朝阳区望京SOHO地下停车场', 116.470092, 40.002096, 15, 15, 1, '00:00-24:00', '400-800-1004', '望京商圈，配备大功率快充桩');

INSERT INTO charging_pile (station_id, pile_no, pile_type, power_rating, status) VALUES
(1, 'CY-001', 1, 120.00, 0), (1, 'CY-002', 1, 120.00, 0), (1, 'CY-003', 1, 120.00, 0), (1, 'CY-004', 1, 120.00, 0),
(1, 'CY-005', 2, 7.00, 0), (1, 'CY-006', 2, 7.00, 0), (1, 'CY-007', 2, 7.00, 0), (1, 'CY-008', 2, 7.00, 0),
(2, 'GM-001', 1, 180.00, 0), (2, 'GM-002', 1, 180.00, 0), (2, 'GM-003', 1, 180.00, 0), (2, 'GM-004', 1, 180.00, 0),
(2, 'GM-005', 1, 180.00, 0), (2, 'GM-006', 1, 180.00, 0), (2, 'GM-007', 2, 7.00, 0), (2, 'GM-008', 2, 7.00, 0),
(2, 'GM-009', 2, 7.00, 0), (2, 'GM-010', 2, 7.00, 0), (2, 'GM-011', 2, 7.00, 0), (2, 'GM-012', 2, 7.00, 0),
(3, 'ZG-001', 1, 120.00, 0), (3, 'ZG-002', 1, 120.00, 0), (3, 'ZG-003', 1, 120.00, 0), (3, 'ZG-004', 1, 120.00, 0),
(3, 'ZG-005', 1, 120.00, 0), (3, 'ZG-006', 2, 7.00, 0), (3, 'ZG-007', 2, 7.00, 0), (3, 'ZG-008', 2, 7.00, 0),
(3, 'ZG-009', 2, 7.00, 0), (3, 'ZG-010', 2, 7.00, 0),
(4, 'WJ-001', 1, 180.00, 0), (4, 'WJ-002', 1, 180.00, 0), (4, 'WJ-003', 1, 180.00, 0), (4, 'WJ-004', 1, 180.00, 0),
(4, 'WJ-005', 1, 180.00, 0), (4, 'WJ-006', 1, 180.00, 0), (4, 'WJ-007', 1, 180.00, 0), (4, 'WJ-008', 1, 180.00, 0),
(4, 'WJ-009', 2, 7.00, 0), (4, 'WJ-010', 2, 7.00, 0), (4, 'WJ-011', 2, 7.00, 0), (4, 'WJ-012', 2, 7.00, 0),
(4, 'WJ-013', 2, 7.00, 0), (4, 'WJ-014', 2, 7.00, 0), (4, 'WJ-015', 2, 7.00, 0);

INSERT INTO user (username, phone, password, license_plate, balance, status) VALUES
('张三', '13800138001', 'e10adc3949ba59abbe56e057f20f883e', '京A12345', 500.00, 1),
('李四', '13800138002', 'e10adc3949ba59abbe56e057f20f883e', '京B67890', 300.00, 1),
('王五', '13800138003', 'e10adc3949ba59abbe56e057f20f883e', '京C11111', 1000.00, 1),
('赵六', '13800138004', 'e10adc3949ba59abbe56e057f20f883e', '京D22222', 200.00, 1);
