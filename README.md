# Fitness OCR 项目

营养成分识别系统，通过 OCR 技术识别食品包装上的营养成分表。

## 项目结构

```
fitness/
├── fitness-frontend/     # 前端项目 (Vue 3 + Vite)
├── fitness-backend/      # 后端项目 (Spring Boot + MySQL)
├── start.sh              # 启动脚本
└── README.md             # 项目说明
```

## 快速开始

### 1. 环境要求

- **前端**: Node.js 16+, npm
- **后端**: JDK 17+, Maven 3.6+, MySQL 8.0+
- **OCR**: RapidOCR 服务 (Python)

### 2. 数据库初始化

```bash
mysql -u root -p < fitness-backend/src/main/resources/db/init.sql
```

### 3. 启动服务

使用启动脚本：

```bash
./start.sh
```

或手动启动：

```bash
# 启动后端
cd fitness-backend
mvn spring-boot:run

# 启动前端
cd fitness-frontend
npm run dev
```

### 4. 启动 OCR 服务

RapidOCR 服务需要单独启动：

```bash
# Python 方式
python rapidocr_server.py --port 8000

# 或使用 Docker
docker run -p 8000:8000 your-rapidocr-image
```

## 服务地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:5173 | Vue 开发服务器 |
| 后端 | http://localhost:8080 | Spring Boot API |
| OCR | http://localhost:8000 | RapidOCR 服务 |

## API 接口

### OCR 识别

```http
POST /api/ocr/recognize
Content-Type: application/json

{
  "imageBase64": "data:image/jpeg;base64,...",
  "parseNutrition": true
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "text": "营养成分表...",
    "nutrition": {
      "calories": "200",
      "protein": "5",
      "fat": "3",
      "carbohydrates": "20"
    }
  }
}
```

## 技术栈

### 前端
- Vue 3
- Vite
- Pinia (状态管理)
- Vue Router

### 后端
- Spring Boot 3.2
- MySQL 8.0
- JPA/Hibernate
- OkHttp (调用 OCR 服务)

## 项目亮点

1. **前后端分离**: 前端专注于 UI，后端处理 OCR 和数据解析
2. **智能解析**: 利用 OCR 文本块的坐标信息，智能匹配营养名称和数值
3. **策略模式**: 前端支持多种 OCR 服务切换（后端、RapidOCR、百度、腾讯）
4. **数据持久化**: 识别记录存入数据库，便于后续分析优化