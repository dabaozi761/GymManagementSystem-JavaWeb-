DROP TABLE IF EXISTS card_transaction;
DROP TABLE IF EXISTS shop_order;
DROP TABLE IF EXISTS shop_product;
DROP TABLE IF EXISTS membership_card;
DROP TABLE IF EXISTS member;
CREATE TABLE member (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, member_no VARCHAR(32), user_id BIGINT UNIQUE,
 name VARCHAR(50), gender INT, phone VARCHAR(20), birthday DATE, join_date DATE,
 status INT DEFAULT 1, remark VARCHAR(255),
 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, del_flag INT DEFAULT 0
);
CREATE TABLE membership_card (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, card_no VARCHAR(32) UNIQUE, member_id BIGINT,
 card_type INT, balance DECIMAL(10,2) NOT NULL DEFAULT 0, total_count INT, remain_count INT,
 start_date DATE, end_date DATE, status INT DEFAULT 1,
 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, del_flag INT DEFAULT 0
);
CREATE TABLE card_transaction (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, transaction_no VARCHAR(40) UNIQUE, card_id BIGINT, card_no VARCHAR(32),
 member_id BIGINT, member_name VARCHAR(50), type INT, amount DECIMAL(10,2), balance_before DECIMAL(10,2), balance_after DECIMAL(10,2),
 count INT, remain_count_before INT, remain_count_after INT, order_id BIGINT, operator_id BIGINT, operator_role INT, remark VARCHAR(255),
 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, del_flag INT DEFAULT 0
);
CREATE TABLE shop_product (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), category INT, price DECIMAL(10,2), stock INT, status INT DEFAULT 1, remark VARCHAR(255),
 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, del_flag INT DEFAULT 0
);
CREATE TABLE shop_order (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, order_no VARCHAR(40) UNIQUE, request_no VARCHAR(64) UNIQUE, member_id BIGINT, member_name VARCHAR(50),
 card_id BIGINT, card_no VARCHAR(32), product_id BIGINT, product_name VARCHAR(100), category INT, unit_price DECIMAL(10,2), quantity INT, rental_days INT,
 amount DECIMAL(10,2), status INT, due_time TIMESTAMP, return_time TIMESTAMP,
 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, del_flag INT DEFAULT 0
);
