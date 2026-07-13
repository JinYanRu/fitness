#!/bin/bash

# 健身 OCR 项目启动脚本

echo "=========================================="
echo "  Fitness OCR 项目启动脚本"
echo "=========================================="

# 检查当前目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# 函数：检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo "❌ $1 未安装，请先安装"
        return 1
    fi
    echo "✓ $1 已安装"
    return 0
}

# 函数：启动后端
start_backend() {
    echo ""
    echo ">>> 启动后端服务..."
    cd fitness-backend

    # 检查 Java 和 Maven
    check_command java
    check_command mvn

    # 检查 MySQL 是否运行
    echo "检查 MySQL 服务..."
    if mysqladmin ping -h localhost -u root --silent 2>/dev/null; then
        echo "✓ MySQL 服务正常"
    else
        echo "⚠ MySQL 服务未启动，请先启动 MySQL"
        echo "  macOS: brew services start mysql"
        echo "  Linux: sudo systemctl start mysql"
    fi

    echo "启动 Spring Boot..."
    mvn spring-boot:run &
    BACKEND_PID=$!
    echo "后端 PID: $BACKEND_PID"

    cd ..
}

# 函数：启动前端
start_frontend() {
    echo ""
    echo ">>> 启动前端服务..."
    cd fitness-frontend

    # 检查 Node 和 npm
    check_command node
    check_command npm

    # 检查依赖是否安装
    if [ ! -d "node_modules" ]; then
        echo "安装前端依赖..."
        npm install
    fi

    echo "启动 Vite 开发服务器..."
    npm run dev &
    FRONTEND_PID=$!
    echo "前端 PID: $FRONTEND_PID"

    cd ..
}

# 函数：启动 RapidOCR 服务（可选）
start_rapidocr() {
    echo ""
    echo ">>> 启动 RapidOCR 服务..."
    echo "⚠ RapidOCR 需要手动启动"
    echo "  请确保 RapidOCR 服务运行在 localhost:8000"
    echo "  或者使用 Docker: docker run -p 8000:8000 rapidocr-server"
}

# 主流程
echo ""
echo "检查环境..."
check_command java || echo "请安装 JDK 17+"
check_command mvn || echo "请安装 Maven"
check_command node || echo "请安装 Node.js"
check_command npm || echo "请安装 npm"

echo ""
read -p "是否启动后端服务？(y/n): " start_backend_choice
if [[ "$start_backend_choice" == "y" ]]; then
    start_backend
fi

echo ""
read -p "是否启动前端服务？(y/n): " start_frontend_choice
if [[ "$start_frontend_choice" == "y" ]]; then
    start_frontend
fi

echo ""
read -p "是否启动 RapidOCR 服务提示？(y/n): " start_rapidocr_choice
if [[ "$start_rapidocr_choice" == "y" ]]; then
    start_rapidocr
fi

echo ""
echo "=========================================="
echo "  服务启动完成"
echo "=========================================="
echo ""
echo "前端地址: http://localhost:5173"
echo "后端地址: http://localhost:8080"
echo "OCR服务:  http://localhost:8000 (需要单独启动)"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

# 等待子进程
wait