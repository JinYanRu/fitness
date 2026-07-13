# 健身营养记录 App

一个基于 UniApp (Vue 3) 开发的食物营养成分记录应用，支持 OCR 识别营养标签。

## 功能特性

- 📷 **图片选择**：支持拍照或从相册选择食物营养标签图片
- 🔍 **OCR 识别**：策略模式支持多厂商 OCR 服务（uTools MCP、百度、腾讯云等）
- 📝 **营养记录**：自动解析并记录营养成分信息
- 💾 **数据存储**：支持 MySQL 云端存储和本地 Storage 备用
- 📊 **统计分析**：查看历史记录和营养数据统计

## 项目结构

```
fitness/
├── src/                          # UniApp 前端源码
│   ├── pages/                    # 页面
│   │   ├── index/                # 首页
│   │   ├── record/               # 记录页面
│   │   └── history/              # 历史记录
│   ├── components/               # 组件
│   │   ├── ImagePicker.vue       # 图片选择
│   │   ├── NutritionForm.vue     # 营养表单
│   │   └── NutritionCard.vue     # 营养卡片
│   ├── services/                 # 服务层
│   │   ├── ocr/                  # OCR 服务（策略模式）
│   │   │   ├── index.js          # OCR 管理器
│   │   │   ├── base.js           # 基础策略类
│   │   │   ├── utools.js         # uTools 实现
│   │   │   ├── baidu.js          # 百度 OCR
│   │   │   ├── tencent.js        # 腾讯云 OCR
│   │   │   └── mock.js           # 模拟 OCR
│   │   └── api/                  # API 服务
│   ├── store/                    # Pinia 状态管理
│   ├── utils/                    # 工具函数
│   └── main.js                   # 入口文件
├── server/                       # Node.js 后端服务
│   ├── app.js                    # Express 服务
│   └── package.json
└── package.json
```

## 技术栈

- **前端**：UniApp + Vue 3 (Composition API) + Pinia
- **后端**：Node.js + Express
- **数据库**：MySQL
- **OCR**：策略模式，支持多厂商

## 快速开始

### 1. 安装依赖

```bash
# 前端
npm install

# 后端（可选）
cd server
npm install
```

### 2. 配置数据库

数据库已创建：`fitness_nutrition`

表结构：
```sql
CREATE TABLE nutrition_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  food_name VARCHAR(100) NOT NULL COMMENT '食物名称',
  food_type VARCHAR(50) COMMENT '食物类型',
  image_path VARCHAR(500) COMMENT '图片路径',
  serving_size DECIMAL(10,2) COMMENT '份量',
  serving_unit VARCHAR(20) COMMENT '单位',
  calories DECIMAL(10,2) COMMENT '热量(kcal)',
  protein DECIMAL(10,2) COMMENT '蛋白质(g)',
  fat DECIMAL(10,2) COMMENT '脂肪(g)',
  carbohydrates DECIMAL(10,2) COMMENT '碳水化合物(g)',
  fiber DECIMAL(10,2) COMMENT '膳食纤维(g)',
  sodium DECIMAL(10,2) COMMENT '钠(mg)',
  sugar DECIMAL(10,2) COMMENT '糖(g)',
  raw_ocr_text TEXT COMMENT '原始OCR识别文本',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3. 配置 OCR 服务

编辑 `src/services/ocr/init.js`，配置你的 OCR 服务：

```javascript
const defaultConfig = {
  defaultStrategy: 'mock', // 默认使用 mock，可切换为 'baidu', 'tencent', 'utools'
  baidu: {
    apiKey: 'your_api_key',
    secretKey: 'your_secret_key'
  },
  tencent: {
    secretId: 'your_secret_id',
    secretKey: 'your_secret_key',
    proxyUrl: 'your_proxy_url'
  },
  utools: {
    apiUrl: 'your_ocr_api_url'
  }
}
```

### 4. 运行项目

```bash
# H5 开发
npm run dev:h5

# 微信小程序
npm run dev:mp-weixin

# App
npm run dev:app

# 后端服务（可选）
cd server
npm run dev
```

## OCR 策略模式

项目采用策略模式设计 OCR 服务，支持灵活切换不同厂商：

```javascript
import ocrManager from '@/services/ocr/init.js'

// 切换 OCR 策略
ocrManager.use('baidu') // 或 'tencent', 'utools', 'mock'

// 识别图片
const result = await ocrManager.recognize(imagePath)
```

### 支持的 OCR 厂商

| 厂商 | 配置项 | 说明 |
|------|--------|------|
| Mock | 无需配置 | 模拟数据，用于开发测试 |
| uTools MCP | apiUrl | 通过 uTools MCP 服务调用 |
| 百度 OCR | apiKey, secretKey | 百度 AI 开放平台 |
| 腾讯云 OCR | secretId, secretKey, proxyUrl | 腾讯云文字识别 |

## API 接口

### 营养记录

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/nutrition | 获取所有记录 |
| GET | /api/nutrition/:id | 获取单条记录 |
| POST | /api/nutrition | 创建记录 |
| PUT | /api/nutrition/:id | 更新记录 |
| DELETE | /api/nutrition/:id | 删除记录 |
| GET | /api/nutrition/stats/today | 获取今日统计 |

## 页面截图

（待补充）

## 开发计划

- [ ] 图片云存储支持
- [ ] 用户登录系统
- [ ] 数据导出功能
- [ ] 营养目标设置
- [ ] 数据可视化图表

## License

MIT