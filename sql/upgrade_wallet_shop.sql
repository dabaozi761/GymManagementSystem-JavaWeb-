-- 已有数据库升级：不删除表和业务数据。执行前备份数据库。
USE `gym_management_system`;

-- 四类卡统一持有余额。旧类型5不会自动改成另一种卡，请管理员按实际权益调整。
UPDATE membership_card SET balance = 0 WHERE balance IS NULL;
ALTER TABLE membership_card
    MODIFY card_type TINYINT NOT NULL COMMENT '卡类型:1月卡 2季卡 3年卡 4次卡',
    MODIFY balance DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '会员卡余额，全部卡类型可用';

CREATE TABLE IF NOT EXISTS card_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    transaction_no VARCHAR(40) NOT NULL UNIQUE,
    card_id BIGINT NOT NULL,
    card_no VARCHAR(32) NOT NULL,
    member_id BIGINT NOT NULL,
    member_name VARCHAR(50) NOT NULL,
    type TINYINT NOT NULL COMMENT '1充值 2余额消费 3次数核销',
    amount DECIMAL(10,2) NOT NULL,
    balance_before DECIMAL(10,2) NOT NULL,
    balance_after DECIMAL(10,2) NOT NULL,
    count INT DEFAULT NULL,
    remain_count_before INT DEFAULT NULL,
    remain_count_after INT DEFAULT NULL,
    order_id BIGINT DEFAULT NULL,
    operator_id BIGINT DEFAULT NULL,
    operator_role TINYINT DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT NOT NULL DEFAULT 0,
    KEY idx_member_type_time (member_id, type, create_time),
    KEY idx_card_time (card_id, create_time),
    KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS shop_product (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category TINYINT NOT NULL COMMENT '1储物柜 2充电宝 3饮料 4蛋白粉 5运动周边',
    price DECIMAL(10,2) NOT NULL COMMENT '普通商品元/件，租用品元/件/天',
    stock INT NOT NULL DEFAULT 0 COMMENT '可售/可租库存，不含在租数量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT NOT NULL DEFAULT 0,
    KEY idx_status_category (status, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS shop_order (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    request_no VARCHAR(64) NOT NULL UNIQUE COMMENT '重复提交去重标识',
    member_id BIGINT NOT NULL,
    member_name VARCHAR(50) NOT NULL,
    card_id BIGINT NOT NULL,
    card_no VARCHAR(32) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    category TINYINT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    rental_days INT NOT NULL DEFAULT 0,
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL COMMENT '1购买完成 2租用中 3已归还',
    due_time DATETIME DEFAULT NULL,
    return_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT NOT NULL DEFAULT 0,
    KEY idx_member_time (member_id, create_time),
    KEY idx_product_status (product_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 以下为示例商品，仅在商品表为空时添加，价格可在管理页面修改。
INSERT INTO shop_product (name, category, price, stock, remark)
SELECT name, category, price, stock, remark FROM (
    SELECT '储物柜租用' name, 1 category, 5.00 price, 30 stock, '按件/天预付，归还后释放库存' remark
    UNION ALL SELECT '充电宝租用', 2, 3.00, 10, '按件/天预付，前台确认归还'
    UNION ALL SELECT '运动饮料', 3, 8.00, 50, '每瓶'
    UNION ALL SELECT '乳清蛋白粉', 4, 199.00, 20, '每罐'
    UNION ALL SELECT '运动毛巾', 5, 25.00, 30, '每条'
) seed WHERE NOT EXISTS (SELECT 1 FROM shop_product);
