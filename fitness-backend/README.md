# Fitness OCR Backend

## 项目简介

后端服务，用于处理 OCR 识别请求，识别食品营养成分表。

## 技术栈

- Spring Boot 3.2.0
- MySQL 8.0+
- Maven
- Lombok

## 项目结构

```
fitness-backend/
├── src/main/java/com/fitness/ocr/
│   ├── FitnessOcrApplication.java    # 主入口
│   ├── config/                        # 配置类
│   │   ├── CorsConfig.java           # 跨域配置
│   │   └── OcrProperties.java        # OCR服务配置
│   ├── controller/                    # 控制器
│   │   └── OcrController.java        # OCR接口
│   ├── dto/                           # 数据传输对象
│   │   ├── OcrRequest.java           # 请求DTO
│   │   ├── OcrResultDTO.java         # 响应DTO
│   │   └── Result.java               # 统一响应
│   ├── entity/                        # 实体类
│   │   └── OcrRecord.java            # OCR记录实体
│   ├── exception/                     # 异常处理
│   │   └── GlobalExceptionHandler.java
│   ├── repository/                    # 数据访问层
│   │   └── OcrRecordRepository.java
│   └── service/                       # 服务层
│       └── OcrService.java           # OCR服务
├── src/main/resources/
│   ├── application.yml               # 应用配置
│   └── db/
│       └── init.sql                  # 数据库初始化脚本
└── pom.xml                           # Maven配置
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- RapidOCR 服务 (运行在 localhost:8000)

### 2. 数据库配置

```sql
-- 执行数据库初始化脚本
source src/main/resources/db/init.sql
```

修改 `application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fitness_ocr?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3. 启动 RapidOCR 服务

确保 RapidOCR 服务正在运行：

```bash
# 如果使用 Python 版本的 RapidOCR 服务
python rapidocr_server.py --port 8000
```

### 4. 构建项目

```bash
cd fitness-backend
mvn clean install
```

### 5. 启动服务

```bash
mvn spring-boot:run
```

服务将在 http://localhost:8080 启动。

## API 接口

### OCR 识别

**POST** `/api/ocr/recognize`

请求体：
```json
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
    "text": "营养成分表\n每100克\n能量 200千焦\n蛋白质 5克\n...",
    "texts": [...],
    "nutrition": {
      "calories": "200",
      "protein": "5",
      "fat": "3",
      "carbohydrates": "20",
      "fiber": "2",
      "sodium": "100",
      "sugar": "5",
      "calcium": "50"
    },
    "elapseMs": 1234
  }
}
```

### 健康检查

**GET** `/api/ocr/health`

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": "OK"
}
```

## 配置说明

### OCR 服务配置

```yaml
ocr:
  service:
    rapid-url: http://localhost:8000  # RapidOCR 服务地址
    timeout: 30000                     # 请求超时时间(毫秒)
```

## 前端对接

前端需要修改 API 请求地址，指向后端服务：

```javascript
// 原来: 直接调用 RapidOCR
// 现在: 调用后端服务
const response = await fetch('http://localhost:8080/api/ocr/recognize', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    imageBase64: base64Image
  })
})
```

## 后续优化方向

1. **提高识别准确率**
   - 图片预处理（去噪、增强对比度）
   - 多次识别取最优结果
   - 结合多个 OCR 引擎

2. **数据积累**
   - 保存识别记录到数据库
   - 人工校验后存入食品营养表
   - 建立食品营养数据库

3. **智能匹配**
   - 根据食品名称匹配已知营养数据
   - 模糊搜索和智能推荐
