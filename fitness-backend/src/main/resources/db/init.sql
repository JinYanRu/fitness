-- 创建数据库
CREATE DATABASE IF NOT EXISTS fitness_ocr DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fitness_ocr;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(64) UNIQUE NOT NULL COMMENT '用户名',
    password_hash VARCHAR(256) NOT NULL COMMENT '密码(加密)',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar VARCHAR(256) COMMENT '头像',
    email VARCHAR(128) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    UNIQUE INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户档案表
CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    gender TINYINT COMMENT '性别: 1-男, 2-女',
    birthday DATE COMMENT '生日',
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    target_weight DECIMAL(5,2) COMMENT '目标体重(kg)',
    goal VARCHAR(32) COMMENT '目标: muscle_gain/fat_loss/maintain',
    activity_level VARCHAR(32) COMMENT '活动水平: sedentary/light/moderate/high',
    bmr DECIMAL(10,2) COMMENT '基础代谢率',
    tdee DECIMAL(10,2) COMMENT '每日总能量消耗',
    target_calories INT COMMENT '目标热量(kcal)',
    target_protein DECIMAL(10,2) COMMENT '目标蛋白质(g)',
    target_fat DECIMAL(10,2) COMMENT '目标脂肪(g)',
    target_carbs DECIMAL(10,2) COMMENT '目标碳水(g)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户档案表';

-- OCR 识别记录表
CREATE TABLE IF NOT EXISTS ocr_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(64) COMMENT '用户ID',
    original_text TEXT COMMENT '原始识别文本',
    full_text TEXT COMMENT '完整文本',
    calories VARCHAR(32) COMMENT '能量/热量',
    protein VARCHAR(32) COMMENT '蛋白质',
    fat VARCHAR(32) COMMENT '脂肪',
    saturated_fat VARCHAR(32) COMMENT '饱和脂肪',
    carbohydrates VARCHAR(32) COMMENT '碳水化合物',
    fiber VARCHAR(32) COMMENT '膳食纤维',
    sodium VARCHAR(32) COMMENT '钠',
    sugar VARCHAR(32) COMMENT '糖',
    calcium VARCHAR(32) COMMENT '钙',
    elapse_ms BIGINT COMMENT '处理耗时(毫秒)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-成功, 0-失败',
    error_msg VARCHAR(512) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OCR识别记录表';

-- 食品营养数据表（用于后续数据积累和优化）
CREATE TABLE IF NOT EXISTS food_nutrition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    food_name VARCHAR(128) NOT NULL COMMENT '食品名称',
    brand VARCHAR(64) COMMENT '品牌',
    serving_size VARCHAR(64) COMMENT '份量',
    calories DECIMAL(10,2) COMMENT '能量(kcal)',
    protein DECIMAL(10,2) COMMENT '蛋白质(g)',
    fat DECIMAL(10,2) COMMENT '脂肪(g)',
    saturated_fat DECIMAL(10,2) COMMENT '饱和脂肪(g)',
    carbohydrates DECIMAL(10,2) COMMENT '碳水化合物(g)',
    fiber DECIMAL(10,2) COMMENT '膳食纤维(g)',
    sodium DECIMAL(10,2) COMMENT '钠(mg)',
    sugar DECIMAL(10,2) COMMENT '糖(g)',
    calcium DECIMAL(10,2) COMMENT '钙(mg)',
    source VARCHAR(32) DEFAULT 'OCR' COMMENT '数据来源',
    verified TINYINT DEFAULT 0 COMMENT '是否已验证: 0-否, 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_food_name (food_name),
    INDEX idx_brand (brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品营养数据表';

-- 营养记录表（用户每日饮食记录）
CREATE TABLE IF NOT EXISTS nutrition_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(64) COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    meal_type VARCHAR(32) COMMENT '用餐类型: breakfast/lunch/dinner/snack/workout',
    food_name VARCHAR(128) NOT NULL COMMENT '食物名称',
    brand VARCHAR(64) COMMENT '品牌',
    serving_amount DECIMAL(10,2) COMMENT '食用份量',
    serving_unit VARCHAR(16) COMMENT '份量单位',
    calories DECIMAL(10,2) COMMENT '能量(kcal)',
    protein DECIMAL(10,2) COMMENT '蛋白质(g)',
    fat DECIMAL(10,2) COMMENT '脂肪(g)',
    saturated_fat DECIMAL(10,2) COMMENT '饱和脂肪(g)',
    carbohydrates DECIMAL(10,2) COMMENT '碳水化合物(g)',
    fiber DECIMAL(10,2) COMMENT '膳食纤维(g)',
    sodium DECIMAL(10,2) COMMENT '钠(mg)',
    sugar DECIMAL(10,2) COMMENT '糖(g)',
    calcium DECIMAL(10,2) COMMENT '钙(mg)',
    remark VARCHAR(256) COMMENT '备注',
    ocr_text TEXT COMMENT '原始OCR文本',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date),
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_user_date_meal (user_id, record_date, meal_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营养记录表';

-- 用户私有食物库
CREATE TABLE IF NOT EXISTS user_food (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    food_name VARCHAR(128) NOT NULL COMMENT '食物名称',
    brand VARCHAR(64) COMMENT '品牌',
    serving_size DECIMAL(10,2) COMMENT '份量',
    serving_unit VARCHAR(16) COMMENT '份量单位',
    calories DECIMAL(10,2) COMMENT '能量(kcal)',
    protein DECIMAL(10,2) COMMENT '蛋白质(g)',
    fat DECIMAL(10,2) COMMENT '脂肪(g)',
    saturated_fat DECIMAL(10,2) COMMENT '饱和脂肪(g)',
    carbohydrates DECIMAL(10,2) COMMENT '碳水化合物(g)',
    fiber DECIMAL(10,2) COMMENT '膳食纤维(g)',
    sodium DECIMAL(10,2) COMMENT '钠(mg)',
    sugar DECIMAL(10,2) COMMENT '糖(g)',
    calcium DECIMAL(10,2) COMMENT '钙(mg)',
    source VARCHAR(32) DEFAULT 'ocr' COMMENT '数据来源: ocr/manual',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_food (user_id, food_name),
    INDEX idx_user_create (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户私有食物库';

-- 公共食物库
CREATE TABLE IF NOT EXISTS common_food (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    food_name VARCHAR(128) NOT NULL COMMENT '食物名称',
    category VARCHAR(32) COMMENT '食物分类',
    brand VARCHAR(64) COMMENT '品牌',
    serving_size DECIMAL(10,2) COMMENT '份量',
    serving_unit VARCHAR(16) COMMENT '份量单位',
    calories DECIMAL(10,2) COMMENT '能量(kcal)',
    protein DECIMAL(10,2) COMMENT '蛋白质(g)',
    fat DECIMAL(10,2) COMMENT '脂肪(g)',
    saturated_fat DECIMAL(10,2) COMMENT '饱和脂肪(g)',
    carbohydrates DECIMAL(10,2) COMMENT '碳水化合物(g)',
    fiber DECIMAL(10,2) COMMENT '膳食纤维(g)',
    sodium DECIMAL(10,2) COMMENT '钠(mg)',
    sugar DECIMAL(10,2) COMMENT '糖(g)',
    calcium DECIMAL(10,2) COMMENT '钙(mg)',
    source VARCHAR(32) DEFAULT 'official' COMMENT '数据来源: official/user_upload/crawled',
    verified TINYINT DEFAULT 0 COMMENT '是否已验证: 0-否, 1-是',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_food_name (food_name),
    INDEX idx_category (category),
    INDEX idx_brand (brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共食物库';

-- 用户菜谱表
CREATE TABLE IF NOT EXISTS user_recipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(128) NOT NULL COMMENT '菜谱名称',
    servings INT DEFAULT 1 COMMENT '份数',
    total_weight DECIMAL(10,2) COMMENT '总重量(g)',
    calories DECIMAL(10,2) COMMENT '能量(kcal)',
    protein DECIMAL(10,2) COMMENT '蛋白质(g)',
    fat DECIMAL(10,2) COMMENT '脂肪(g)',
    saturated_fat DECIMAL(10,2) COMMENT '饱和脂肪(g)',
    carbohydrates DECIMAL(10,2) COMMENT '碳水化合物(g)',
    fiber DECIMAL(10,2) COMMENT '膳食纤维(g)',
    sodium DECIMAL(10,2) COMMENT '钠(mg)',
    sugar DECIMAL(10,2) COMMENT '糖(g)',
    calcium DECIMAL(10,2) COMMENT '钙(mg)',
    remark TEXT COMMENT '备注/做法',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_recipe (user_id, name),
    INDEX idx_user_create (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户菜谱表';

-- 菜谱原料表
CREATE TABLE IF NOT EXISTS recipe_ingredient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    recipe_id BIGINT NOT NULL COMMENT '菜谱ID',
    food_type VARCHAR(32) COMMENT '食物类型: common/user',
    food_id BIGINT COMMENT '食物ID',
    food_name VARCHAR(128) COMMENT '食物名称',
    amount DECIMAL(10,2) COMMENT '用量',
    unit VARCHAR(16) COMMENT '单位',
    calories DECIMAL(10,2) COMMENT '能量(kcal)',
    protein DECIMAL(10,2) COMMENT '蛋白质(g)',
    fat DECIMAL(10,2) COMMENT '脂肪(g)',
    carbohydrates DECIMAL(10,2) COMMENT '碳水化合物(g)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_recipe (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱原料表';