#!/bin/bash

# ============================================
#  Fitness OCR 自动化发布脚本
#  本地打包 → 上传服务器 → 重启服务
#
#  用法:
#    ./deploy.sh              # 完整部署（前端+后端）
#    ./deploy.sh frontend     # 仅部署前端
#    ./deploy.sh backend      # 仅部署后端
#    ./deploy.sh rollback     # 回滚到上一版本
# ============================================

set -e

# ---- 配置区 ----
REMOTE_HOST="111.228.49.250"
REMOTE_PORT="10260"
REMOTE_USER="raza"
REMOTE_PASS="JinYanru(()!)("
REMOTE_DIR="/home/raza/server/fitness"
JAR_NAME="fitness-ocr-1.0.0.jar"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step()  { echo -e "\n${BLUE}>>> $1${NC}"; }

# 项目根目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# 时间戳（用于备份）
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# ---- 函数区 ----

# 检查本地环境
check_local_env() {
    log_step "检查本地环境"
    local missing=0

    if ! command -v java &> /dev/null; then
        log_error "Java 未安装"
        missing=1
    else
        log_info "Java: $(java -version 2>&1 | head -1)"
    fi

    if ! command -v mvn &> /dev/null; then
        log_error "Maven 未安装"
        missing=1
    else
        log_info "Maven: $(mvn -version | head -1)"
    fi

    if ! command -v node &> /dev/null; then
        log_error "Node.js 未安装"
        missing=1
    else
        log_info "Node: $(node -v)"
    fi

    if ! command -v npm &> /dev/null; then
        log_error "npm 未安装"
        missing=1
    else
        log_info "npm: $(npm -v)"
    fi

    if [ $missing -eq 1 ]; then
        log_error "缺少必要工具，请先安装"
        exit 1
    fi
}

# 检查远程服务器连接
check_remote() {
    log_step "检查远程服务器连接"
    if sshpass -p "${REMOTE_PASS}" ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} "echo '连接成功'" &> /dev/null; then
        log_info "服务器 ${REMOTE_HOST} 连接正常"
    else
        log_error "无法连接服务器 ${REMOTE_HOST}"
        log_warn "请检查: 1) 服务器是否可达  2) SSH 密钥或密码是否正确  3) 安装 sshpass 以使用密码认证"
        exit 1
    fi
}

# 打包前端
build_frontend() {
    log_step "打包前端"
    cd "$PROJECT_DIR/fitness-frontend"

    # 安装依赖
    if [ ! -d "node_modules" ] || [ package.json -nt node_modules/.package-lock.json ]; then
        log_info "安装前端依赖..."
        npm install
    fi

    # 构建
    log_info "构建前端 (vite build)..."
    npm run build:h5

    # 检查构建结果
    if [ ! -f "dist/index.html" ]; then
        log_error "前端构建失败: dist/index.html 不存在"
        exit 1
    fi

    local size=$(du -sh dist/ | cut -f1)
    log_info "前端构建完成 ✓ (大小: ${size})"
    cd "$PROJECT_DIR"
}

# 打包后端
build_backend() {
    log_step "打包后端"
    cd "$PROJECT_DIR/fitness-backend"

    log_info "构建后端 (mvn package)..."
    mvn package -Dmaven.test.skip=true -q

    # 检查构建结果
    if [ ! -f "target/${JAR_NAME}" ]; then
        log_error "后端构建失败: target/${JAR_NAME} 不存在"
        exit 1
    fi

    local size=$(du -sh "target/${JAR_NAME}" | cut -f1)
    log_info "后端构建完成 ✓ (大小: ${size})"
    cd "$PROJECT_DIR"
}

# 备份远程文件
backup_remote() {
    log_step "备份远程文件"
    sshpass -p "${REMOTE_PASS}" ssh -o StrictHostKeyChecking=no -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} "
        cd ${REMOTE_DIR}
        # 备份后端 jar
        if [ -f backend/${JAR_NAME} ]; then
            cp backend/${JAR_NAME} backup/${JAR_NAME}.${TIMESTAMP}
            echo '已备份: backend/${JAR_NAME}'
        fi
        # 备份前端 dist
        if [ -d dist ] && [ \$(ls -A dist 2>/dev/null) ]; then
            tar czf backup/dist_${TIMESTAMP}.tar.gz -C dist .
            echo '已备份: dist/'
        fi
        # 清理旧备份（保留最近 5 个）
        cd backup
        ls -t *.jar.* 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null
        ls -t dist_*.tar.gz 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null
        echo '旧备份已清理'
    "
    log_info "备份完成 ✓"
}

# 上传前端
upload_frontend() {
    log_step "上传前端文件"
    # 确保目录存在
    sshpass -p "${REMOTE_PASS}" ssh -o StrictHostKeyChecking=no -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} "mkdir -p ${REMOTE_DIR}/dist"
    # 清空远程 dist
    sshpass -p "${REMOTE_PASS}" ssh -o StrictHostKeyChecking=no -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} "rm -rf ${REMOTE_DIR}/dist/* || true"
    # 上传整个目录
    sshpass -p "${REMOTE_PASS}" scp -o StrictHostKeyChecking=no -P ${REMOTE_PORT} -r "$PROJECT_DIR/fitness-frontend/dist" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/"
    log_info "前端文件上传完成 ✓"
}

# 上传后端
upload_backend() {
    log_step "上传后端 jar"
    sshpass -p "${REMOTE_PASS}" scp -o StrictHostKeyChecking=no -P ${REMOTE_PORT} "$PROJECT_DIR/fitness-backend/target/${JAR_NAME}" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/backend/${JAR_NAME}"
    log_info "后端 jar 上传完成 ✓"
}

# 重启后端服务
restart_backend() {
    log_step "重启后端服务"
    sshpass -p "${REMOTE_PASS}" ssh -o StrictHostKeyChecking=no -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} "
        # 停止旧进程
        PID=\$(lsof -ti:8080 2>/dev/null || true)
        if [ -n \"\$PID\" ]; then
            echo '停止旧进程 (PID: '\$PID')...'
            kill \$PID 2>/dev/null
            sleep 3
            # 强制杀死
            PID2=\$(lsof -ti:8080 2>/dev/null || true)
            if [ -n \"\$PID2\" ]; then
                kill -9 \$PID2 2>/dev/null
                sleep 1
            fi
        fi

        # 启动新进程
        echo '启动后端服务...'
        cd ${REMOTE_DIR}/backend
        nohup java -jar ${JAR_NAME} \
            > ${REMOTE_DIR}/logs/backend_${TIMESTAMP}.log 2>&1 &

        NEW_PID=\$!
        echo '后端进程 PID: '\$NEW_PID

        # 等待启动
        echo '等待服务启动...'
        for i in \$(seq 1 30); do
            if curl -s http://127.0.0.1:8080/api/health &> /dev/null || curl -s -o /dev/null -w '' http://127.0.0.1:8080/ &> /dev/null; then
                echo '✓ 后端服务启动成功'
                break
            fi
            if [ \$i -eq 30 ]; then
                echo '⚠ 等待超时，请手动检查日志: ${REMOTE_DIR}/logs/backend_${TIMESTAMP}.log'
            fi
            sleep 2
        done

        # 清理旧日志（保留最近 10 个）
        cd ${REMOTE_DIR}/logs
        ls -t backend_*.log 2>/dev/null | tail -n +11 | xargs rm -f 2>/dev/null
    "
    log_info "后端服务重启完成 ✓"
}

# 重载 nginx
reload_nginx() {
    log_step "重载 nginx"
    sshpass -p "${REMOTE_PASS}" ssh -o StrictHostKeyChecking=no -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} "
        nginx -t 2>&1 && (systemctl reload nginx 2>/dev/null || nginx -s reload 2>/dev/null)
        echo '✓ nginx 重载完成'
    "
    log_info "nginx 重载完成 ✓"
}

# 回滚
rollback() {
    log_step "回滚到上一版本"
    sshpass -p "${REMOTE_PASS}" ssh -o StrictHostKeyChecking=no -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} "
        cd ${REMOTE_DIR}

        # 查找最新备份
        LATEST_JAR_BACKUP=\$(ls -t backup/${JAR_NAME}.* 2>/dev/null | head -1)
        LATEST_DIST_BACKUP=\$(ls -t backup/dist_*.tar.gz 2>/dev/null | head -1)

        if [ -z \"\$LATEST_JAR_BACKUP\" ] && [ -z \"\$LATEST_DIST_BACKUP\" ]; then
            echo '❌ 没有可用的备份'
            exit 1
        fi

        # 回滚后端
        if [ -n \"\$LATEST_JAR_BACKUP\" ]; then
            echo '回滚后端: '\$LATEST_JAR_BACKUP
            cp \$LATEST_JAR_BACKUP backend/${JAR_NAME}
        fi

        # 回滚前端
        if [ -n \"\$LATEST_DIST_BACKUP\" ]; then
            echo '回滚前端: '\$LATEST_DIST_BACKUP
            rm -rf dist/*
            tar xzf \$LATEST_DIST_BACKUP -C dist/
        fi

        echo '✓ 回滚完成'
    "

    # 重启后端
    restart_backend
    reload_nginx

    log_info "回滚完成 ✓"
}

# 显示部署状态
show_status() {
    log_step "部署状态"
    sshpass -p "${REMOTE_PASS}" ssh -o StrictHostKeyChecking=no -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} "
        echo '--- 后端服务 ---'
        PID=\$(lsof -ti:8080 2>/dev/null || echo '未运行')
        echo \"PID: \$PID\"
        if [ \"\$PID\" != '未运行' ]; then
            echo \"内存: \$(ps -o rss= -p \$PID 2>/dev/null | awk '{printf \"%.1f MB\", \$1/1024}')\"
        fi

        echo ''
        echo '--- 磁盘使用 ---'
        du -sh ${REMOTE_DIR}/dist 2>/dev/null || echo 'dist: 不存在'
        du -sh ${REMOTE_DIR}/backend 2>/dev/null || echo 'backend: 不存在'
        du -sh ${REMOTE_DIR}/backup 2>/dev/null || echo 'backup: 不存在'

        echo ''
        echo '--- nginx ---'
        nginx -t 2>&1 | tail -1

        echo ''
        echo '--- 最近日志 ---'
        tail -5 ${REMOTE_DIR}/logs/backend_*.log 2>/dev/null | tail -5 || echo '无日志'
    "
}

# ---- 主流程 ----

DEPLOY_TARGET="${1:-all}"

echo "=========================================="
echo "  Fitness OCR 自动化发布"
echo "  目标: ${REMOTE_USER}@${REMOTE_HOST}"
echo "  模式: ${DEPLOY_TARGET}"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

case "$DEPLOY_TARGET" in
    frontend)
        check_local_env
        check_remote
        build_frontend
        backup_remote
        upload_frontend
        reload_nginx
        ;;
    backend)
        check_local_env
        check_remote
        build_backend
        backup_remote
        upload_backend
        restart_backend
        ;;
    all)
        check_local_env
        check_remote
        build_frontend
        build_backend
        backup_remote
        upload_frontend
        upload_backend
        restart_backend
        reload_nginx
        ;;
    rollback)
        check_remote
        rollback
        ;;
    status)
        check_remote
        show_status
        ;;
    *)
        echo "用法: $0 {all|frontend|backend|rollback|status}"
        echo ""
        echo "  all       完整部署（前端+后端）"
        echo "  frontend  仅部署前端"
        echo "  backend   仅部署后端"
        echo "  rollback  回滚到上一版本"
        echo "  status    查看部署状态"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
echo ""
echo "  访问地址: http://${REMOTE_HOST}"
echo "  后端 API: http://${REMOTE_HOST}/api"
echo ""
echo "  查看日志: ssh -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST} 'tail -f ${REMOTE_DIR}/logs/backend_*.log'"
echo "  查看状态: $0 status"
echo ""
