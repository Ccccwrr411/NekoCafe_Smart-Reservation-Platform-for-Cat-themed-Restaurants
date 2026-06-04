-- ==========================================
-- 1. 基础用户信息与权限模块
-- ==========================================

-- 用户基础表
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    nickname VARCHAR(50),
    avatar_url VARCHAR(255),
    real_name VARCHAR(50),
    id_card VARCHAR(20),
    is_verified BOOLEAN DEFAULT FALSE,
    status SMALLINT DEFAULT 1, -- 1:正常 0:禁用
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 角色字典表
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

-- 用户角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    store_id INT, -- 可为空，为空代表全局角色
    PRIMARY KEY (user_id, role_id)
);

-- 会员扩展信息表
CREATE TABLE member_ext (
    user_id BIGINT PRIMARY KEY,
    level INT DEFAULT 1,
    total_points INT DEFAULT 0,
    cumulative_amount NUMERIC(10,2) DEFAULT 0.00,
    last_visit_time TIMESTAMP,
    preferences TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 2. 门店与桌位资源模块
-- ==========================================

-- 门店表
CREATE TABLE stores (
    store_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(50),
    address VARCHAR(255),
    longitude NUMERIC(10,6),
    latitude NUMERIC(10,6),
    contact_phone VARCHAR(20),
    business_hours VARCHAR(100),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 桌位表
CREATE TABLE tables (
    table_id SERIAL PRIMARY KEY,
    store_id INT NOT NULL,
    table_no VARCHAR(20) NOT NULL,
    capacity INT DEFAULT 2,
    table_type VARCHAR(50),
    cat_theme VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE
);

-- 桌位实时状态表
CREATE TABLE table_status (
    table_id INT PRIMARY KEY,
    status VARCHAR(20) DEFAULT 'IDLE', -- IDLE, OCCUPIED, RESERVED, CLEANING
    current_reservation_id BIGINT,
    version INT DEFAULT 0 -- 用于乐观锁防并发
);


-- ==========================================
-- 3. 核心交易与订单模块
-- ==========================================

-- 预约订单表
CREATE TABLE reservations (
    reservation_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id INT NOT NULL,
    table_id INT,
    cat_profile_id INT,
    reservation_time TIMESTAMP NOT NULL,
    duration_min INT DEFAULT 120,
    party_size INT DEFAULT 1,
    special_request TEXT,
    order_amount NUMERIC(10,2) DEFAULT 0.00,
    total_amount NUMERIC(10,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CONFIRMED, COMPLETED, CANCELLED
    points_used INT DEFAULT 0,
    points_earned INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 菜品表
CREATE TABLE dishes (
    dish_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    price NUMERIC(10,2) NOT NULL,
    image_url VARCHAR(255),
    description TEXT,
    tags VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

-- 门店菜品供应表
CREATE TABLE store_dishes (
    store_id INT NOT NULL,
    dish_id INT NOT NULL,
    price_override NUMERIC(10,2),
    is_available BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (store_id, dish_id)
);

-- 订单明细表
CREATE TABLE order_items (
    item_id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    dish_id INT NOT NULL,
    quantity INT DEFAULT 1,
    unit_price NUMERIC(10,2) NOT NULL,
    subtotal NUMERIC(10,2) NOT NULL
);

-- 支付记录表
CREATE TABLE payments (
    payment_id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    payment_method VARCHAR(50), -- WECHAT, ALIPAY, CARD
    amount NUMERIC(10,2) NOT NULL,
    transaction_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'UNPAID',
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 退款明细表
CREATE TABLE refund_records (
    refund_id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    refund_amount NUMERIC(10,2) NOT NULL,
    refund_reason VARCHAR(255),
    refund_transaction_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PROCESSING',
    operator_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);


-- ==========================================
-- 4. 猫咪专项管理（特色模块）
-- ==========================================

-- 猫咪档案表
CREATE TABLE cat_profiles (
    cat_id SERIAL PRIMARY KEY,
    user_id BIGINT, -- 如果是顾客的猫则有关联，店内猫为空
    name VARCHAR(50) NOT NULL,
    breed VARCHAR(50),
    personality VARCHAR(100),
    birth_date DATE,
    weight_kg NUMERIC(5,2),
    avatar_url VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 猫咪健康打卡记录表
CREATE TABLE cat_health_records (
    record_id BIGSERIAL PRIMARY KEY,
    cat_id INT NOT NULL,
    record_type VARCHAR(50), -- VACCINE, CHECKUP, WEIGHT
    record_value VARCHAR(100),
    record_date DATE NOT NULL,
    note TEXT,
    staff_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 5. 营销与现场排队模块
-- ==========================================

-- 活动/优惠券定义表
CREATE TABLE promotions (
    promo_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50), -- DISCOUNT, VOUCHER
    rule_json JSONB, -- PG特有的JSONB格式，极度灵活
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    applicable_stores JSONB,
    is_active BOOLEAN DEFAULT TRUE
);

-- 用户领取的优惠券表
CREATE TABLE user_coupons (
    coupon_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    promo_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'UNUSED', -- UNUSED, USED, EXPIRED
    used_at TIMESTAMP,
    used_reservation_id BIGINT,
    expire_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 优惠券使用记录表
CREATE TABLE coupon_usage (
    usage_id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    discount_amount NUMERIC(10,2) NOT NULL,
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 现场排队表
CREATE TABLE queue (
    queue_id BIGSERIAL PRIMARY KEY,
    store_id INT NOT NULL,
    user_id BIGINT,
    party_size INT NOT NULL,
    preferred_table_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'WAITING', -- WAITING, CALLED, SEATED, CANCELLED
    queue_number VARCHAR(20) NOT NULL,
    called_at TIMESTAMP,
    seated_table_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ==========================================
-- 6. 统计、日志与员工排班
-- ==========================================

-- 门店每日经营统计表
CREATE TABLE store_daily_stats (
    stat_date DATE NOT NULL,
    store_id INT NOT NULL,
    total_reservations INT DEFAULT 0,
    total_revenue NUMERIC(10,2) DEFAULT 0.00,
    table_turnover_rate NUMERIC(5,2),
    revenue_per_seat NUMERIC(10,2),
    repeat_customers INT DEFAULT 0,
    PRIMARY KEY (stat_date, store_id)
);

-- 积分变动日志表
CREATE TABLE points_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    balance_after INT NOT NULL,
    source VARCHAR(50),
    reservation_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 班次字典表
CREATE TABLE staff_shifts (
    shift_id SERIAL PRIMARY KEY,
    shift_name VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- 员工排班表
CREATE TABLE staff_schedules (
    schedule_id BIGSERIAL PRIMARY KEY,
    store_id INT NOT NULL,
    staff_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    shift_id INT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    position VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 调班/请假记录表
CREATE TABLE shift_exceptions (
    exception_id BIGSERIAL PRIMARY KEY,
    store_id INT NOT NULL,
    staff_id BIGINT NOT NULL,
    exception_date DATE NOT NULL,
    type VARCHAR(50), -- LEAVE, SWAP
    original_schedule_id BIGINT,
    new_schedule_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    approver_id BIGINT,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志表
CREATE TABLE operation_logs (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    role_at_time VARCHAR(50),
    operation_type VARCHAR(50),
    target_type VARCHAR(50),
    target_id VARCHAR(50),
    request_ip VARCHAR(50),
    user_agent VARCHAR(255),
    request_params JSONB,
    result VARCHAR(20),
    error_msg TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 评价表
CREATE TABLE reviews (
    review_id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    store_id INT NOT NULL,
    overall_rating INT CHECK (overall_rating >= 1 AND overall_rating <= 5),
    food_rating INT,
    service_rating INT,
    environment_rating INT,
    cat_interaction_rating INT,
    content TEXT,
    images JSONB,
    reply TEXT,
    reply_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'VISIBLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
