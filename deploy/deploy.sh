#!/bin/bash

# ============================================
#  Fitness OCR 自动化发布脚本（Docker 版）
#  本地打包 -> 上传 Windows/WSL2 服务器 -> Docker 部署
#
#  目标服务器: Windows 11 + WSL2 Ubuntu 22.04 + Docker
#
#  用法:
#    ./deploy.sh              # 完整部署（前端+后端）
#    ./deploy.sh frontend     # 仅部署前端
#    ./deploy.sh backend      # 仅部署后端
#    ./deploy.sh rollback     # 回滚到上一版本
#    ./deploy.sh status       # 查看部署状态
# ============================================

set -e

# ---- 配置区 ----
REMOTE_HOST="172.16.11.155"
REMOTE_PORT="22"
REMOTE_USER="pc-admin"
SSH_KEY="$HOME/.ssh/win_admin"
WSL_DISTRO="Ubuntu-22.04"

# 远程路径（WSL2 内）
REMOTE_DIR="/opt/services/fitness"
NGINX_HTML_DIR="/opt/services/nginx/html"
NGINX_CONF_DIR="/opt/services/nginx/conf.d"
JAR_NAME="fitness-ocr-1.0.0.jar"

# MySQL 配置
MYSQL_CONTAINER="mysql"
MYSQL_ROOT_PASSWORD="AJmGYMpJuptTIOsZAP5B"
MYSQL_DATABASE="fitness_ocr"

# Docker 容器名
BACKEND_CONTAINER="fitness-backend"

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
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# ---- 辅助函数 ----

# 在远程 WSL2 中执行命令（base64 编码避免引号问题）
wsl_exec() {
    local cmd="$1"
    local encoded
    encoded=$(printf '%s' "$cmd" | base64 | tr -d '\n')
    ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no -p "$REMOTE_PORT" \
        "$REMOTE_USER@$REMOTE_HOST" "wsl -d $WSL_DISTRO -- bash -lc 'echo $encoded | base64 -d | bash'"
}

# 上传文件到远程 WSL2（通过 SSH 管道）
upload_file() {
    local local_file="$1"
    local remote_path="$2"
    cat "$local_file" | ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no -p "$REMOTE_PORT" \
        "$REMOTE_USER@$REMOTE_HOST" "wsl -d $WSL_DISTRO -- bash -lc 'cat > \"${remote_path}\"'"
}

# ---- 检查函数 ----

check_local_env() {
    log_step "检查本地环境"
    local missing=0

    for tool in java mvn node npm; do
        if ! command -v "$tool" &> /dev/null; then
            log_error "$(echo "$tool" | tr 'a-z' 'A-Z') 未安装"
            missing=1
        fi
    done

    if [ $missing -eq 1 ]; then
        log_error "缺少必要工具，请先安装"
        exit 1
    fi

    log_info "Java: $(java -version 2>&1 | head -1)"
    log_info "Maven: $(mvn -version 2>&1 | head -1)"
    log_info "Node: $(node -v)"
    log_info "npm: $(npm -v)"
}

check_remote() {
    log_step "检查远程服务器连接"
    if [ ! -f "$SSH_KEY" ]; then
        log_error "SSH 密钥不存在: $SSH_KEY"
        exit 1
    fi
    if wsl_exec "echo 'connected'" &> /dev/null; then
        log_info "服务器 ${REMOTE_HOST} (WSL2) 连接正常"
    else
        log_error "无法连接服务器 ${REMOTE_HOST}"
        log_warn "请检查: 1) 服务器是否可达  2) SSH 密钥 $SSH_KEY 是否正确  3) WSL2 是否运行"
        exit 1
    fi
}

# ---- 构建函数 ----

build_frontend() {
    log_step "打包前端"
    cd "$PROJECT_DIR/fitness-frontend"

    if [ ! -d "node_modules" ] || [ package.json -nt node_modules/.package-lock.json ]; then
        log_info "安装前端依赖..."
        npm install
    fi

    log_info "构建前端 (vite build)..."
    npm run build:h5

    if [ ! -f "dist/index.html" ]; then
        log_error "前端构建失败: dist/index.html 不存在"
        exit 1
    fi

    local size=$(du -sh dist/ | cut -f1)
    log_info "前端构建完成 ✓ (大小: ${size})"
    cd "$PROJECT_DIR"
}

build_backend() {
    log_step "打包后端"
    cd "$PROJECT_DIR/fitness-backend"

    log_info "构建后端 (mvn package)..."
    mvn package -Dmaven.test.skip=true -q

    if [ ! -f "target/${JAR_NAME}" ]; then
        log_error "后端构建失败: target/${JAR_NAME} 不存在"
        exit 1
    fi

    local size=$(du -sh "target/${JAR_NAME}" | cut -f1)
    log_info "后端构建完成 ✓ (大小: ${size})"
    cd "$PROJECT_DIR"
}

# ---- 部署函数 ----

init_remote() {
    log_step "初始化远程目录"
    wsl_exec "mkdir -p ${REMOTE_DIR}/backup ${REMOTE_DIR}/logs"
    log_info "远程目录就绪 ✓"
}

init_mysql() {
    log_step "初始化 MySQL 数据库"
    wsl_exec "docker exec ${MYSQL_CONTAINER} mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e 'CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1 | grep -v 'Warning' || true"
    log_info "数据库 ${MYSQL_DATABASE} 就绪 ✓"
}

backup_remote() {
    log_step "备份远程文件"
    wsl_exec "
        cd ${REMOTE_DIR}
        if [ -f ${JAR_NAME} ]; then
            cp ${JAR_NAME} backup/${JAR_NAME}.${TIMESTAMP}
            echo '已备份: ${JAR_NAME}'
        fi
        if [ -d '${NGINX_HTML_DIR}' ] && [ \$(ls -A '${NGINX_HTML_DIR}' 2>/dev/null) ]; then
            tar czf backup/dist_${TIMESTAMP}.tar.gz -C '${NGINX_HTML_DIR}' .
            echo '已备份: dist/'
        fi
        cd backup
        ls -t ${JAR_NAME}.* 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null
        ls -t dist_*.tar.gz 2>/dev/null | tail -n +6 | xargs rm -f 2>/dev/null
        echo '旧备份已清理'
    "
    log_info "备份完成 ✓"
}

upload_frontend() {
    log_step "上传前端文件"
    COPYFILE_DISABLE=1 tar czf - -C "$PROJECT_DIR/fitness-frontend/dist" . | \
        ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no -p "$REMOTE_PORT" \
        "$REMOTE_USER@$REMOTE_HOST" "wsl -d $WSL_DISTRO -- bash -lc 'mkdir -p ${NGINX_HTML_DIR} && find ${NGINX_HTML_DIR} -mindepth 1 -delete && tar xzf - -C ${NGINX_HTML_DIR}'"
    log_info "前端上传完成 ✓"
}

upload_backend() {
    log_step "上传后端文件"
    log_info "上传 JAR..."
    upload_file "$PROJECT_DIR/fitness-backend/target/${JAR_NAME}" "${REMOTE_DIR}/${JAR_NAME}"
    log_info "上传 Dockerfile..."
    upload_file "$SCRIPT_DIR/Dockerfile" "${REMOTE_DIR}/Dockerfile"
    log_info "上传 docker-compose.yml..."
    upload_file "$SCRIPT_DIR/docker-compose.yml" "${REMOTE_DIR}/docker-compose.yml"
    log_info "后端文件上传完成 ✓"
}

restart_backend() {
    log_step "构建并重启后端容器"
    wsl_exec "cd ${REMOTE_DIR} && docker compose down 2>/dev/null; docker compose build 2>&1 && docker compose up -d 2>&1"

    log_info "等待后端启动..."
    for i in $(seq 1 30); do
        if wsl_exec "curl -s -o /dev/null http://127.0.0.1:8080/api/health 2>/dev/null || curl -s -o /dev/null http://127.0.0.1:8080/ 2>/dev/null" 2>/dev/null; then
            log_info "后端服务启动成功 ✓"
            break
        fi
        if [ "$i" -eq 30 ]; then
            log_warn "等待超时，请检查: docker logs ${BACKEND_CONTAINER}"
        fi
        sleep 2
    done
}

reload_nginx() {
    log_step "更新 nginx 配置并重载"
    upload_file "$SCRIPT_DIR/nginx/fitness-docker.conf" "${NGINX_CONF_DIR}/default.conf"
    wsl_exec "docker exec nginx nginx -t 2>&1 && docker exec nginx nginx -s reload 2>&1"
    log_info "nginx 重载完成 ✓"
}

# ---- 回滚 ----

rollback() {
    log_step "回滚到上一版本"
    wsl_exec "
        cd ${REMOTE_DIR}

        LATEST_JAR_BACKUP=\$(ls -t backup/${JAR_NAME}.* 2>/dev/null | head -1)
        LATEST_DIST_BACKUP=\$(ls -t backup/dist_*.tar.gz 2>/dev/null | head -1)

        if [ -z \"\$LATEST_JAR_BACKUP\" ] && [ -z \"\$LATEST_DIST_BACKUP\" ]; then
            echo '没有可用的备份'
            exit 1
        fi

        if [ -n \"\$LATEST_JAR_BACKUP\" ]; then
            echo \"回滚后端: \$LATEST_JAR_BACKUP\"
            cp \$LATEST_JAR_BACKUP ${JAR_NAME}
        fi

        if [ -n \"\$LATEST_DIST_BACKUP\" ]; then
            echo \"回滚前端: \$LATEST_DIST_BACKUP\"
            find '${NGINX_HTML_DIR}' -mindepth 1 -delete 2>/dev/null
            tar xzf \$LATEST_DIST_BACKUP -C '${NGINX_HTML_DIR}'
        fi

        echo '回滚文件已恢复'
    "

    if wsl_exec "test -f ${REMOTE_DIR}/${JAR_NAME}" 2>/dev/null; then
        restart_backend
    fi
    reload_nginx
    log_info "回滚完成 ✓"
}

# ---- 状态 ----

show_status() {
    log_step "部署状态"
    wsl_exec "
        echo '--- 后端容器 ---'
        docker ps --filter name=${BACKEND_CONTAINER} --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' 2>/dev/null || echo '未运行'
        echo ''
        echo '--- 容器日志（最近10行）---'
        docker logs --tail 10 ${BACKEND_CONTAINER} 2>&1 || echo '无日志'
        echo ''
        echo '--- 磁盘使用 ---'
        du -sh ${REMOTE_DIR} 2>/dev/null || echo 'fitness: 不存在'
        du -sh '${NGINX_HTML_DIR}' 2>/dev/null || echo 'nginx html: 不存在'
        echo ''
        echo '--- nginx ---'
        docker exec nginx nginx -t 2>&1 | tail -1
        echo ''
        echo '--- 健康检查 ---'
        curl -s http://127.0.0.1/health 2>&1 || echo 'nginx: 未响应'
        echo ''
        curl -s -o /dev/null -w 'backend HTTP %{http_code}' http://127.0.0.1:8080/api/health 2>&1 || echo 'backend: 未响应'
    "
}

# ---- 主流程 ----

DEPLOY_TARGET="${1:-all}"

echo "=========================================="
echo "  Fitness OCR 自动化发布（Docker 版）"
echo "  目标: ${REMOTE_USER}@${REMOTE_HOST} (WSL2)"
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
        init_remote
        init_mysql
        backup_remote
        upload_backend
        restart_backend
        ;;
    all)
        check_local_env
        check_remote
        build_frontend
        build_backend
        init_remote
        init_mysql
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
echo "  查看日志: ssh -i ${SSH_KEY} ${REMOTE_USER}@${REMOTE_HOST} 'wsl -d ${WSL_DISTRO} -- docker logs -f ${BACKEND_CONTAINER}'"
echo "  查看状态: $0 status"
echo ""
