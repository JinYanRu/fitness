#!/bin/bash

# ============================================
#  远程服务器初始化脚本（只需执行一次）
#  用法: ./remote-init.sh
# ============================================

set -e

# 服务器配置
REMOTE_HOST="111.228.49.250"
REMOTE_PORT="10260"
REMOTE_USER="raza"
REMOTE_DIR="/home/raza/server/fitness"

# 将密码导出为环境变量，避免特殊字符在命令行参数中被 shell 错误解析
export SSHPASS='JinYanru(()!)('

echo "=========================================="
echo "  远程服务器初始化"
echo "  服务器: ${REMOTE_USER}@${REMOTE_HOST}"
echo "=========================================="

# 检查 sshpass 是否安装
if ! command -v sshpass &> /dev/null; then
    echo "⚠ sshpass 未安装，将使用 SSH 密钥认证"
    echo "  如需密码认证，请安装: brew install sshpass (macOS)"
    SSH_CMD="ssh -p ${REMOTE_PORT} ${REMOTE_USER}@${REMOTE_HOST}"
    SCP_CMD="scp -P ${REMOTE_PORT}"
else
    SSH_CMD="sshpass -e ssh -o StrictHostKeyChecking=no -p 10260 ${REMOTE_USER}@${REMOTE_HOST}"
    SCP_CMD="sshpass -e scp -o StrictHostKeyChecking=no -P 10260"
fi

echo ""
echo ">>> 1. 创建目录结构..."
eval $SSH_CMD "mkdir -p ${REMOTE_DIR}/{dist,backend,logs,backup}"

echo "✓ 目录创建完成"
echo "  - ${REMOTE_DIR}/dist      (前端静态文件)"
echo "  - ${REMOTE_DIR}/backend   (后端 jar)"
echo "  - ${REMOTE_DIR}/logs      (日志)"
echo "  - ${REMOTE_DIR}/backup    (备份)"

echo ""
echo ">>> 2. 检查 nginx..."
eval $SSH_CMD "
if command -v nginx &> /dev/null; then
    echo '✓ nginx 已安装'
    nginx -v 2>&1
else
    echo '⚠ nginx 未安装'
fi
"

echo ""
echo ">>> 3. 检查 Java 环境..."
eval $SSH_CMD "
if command -v java &> /dev/null; then
    echo '✓ Java 已安装'
    java -version 2>&1
else
    echo '⚠ Java 未安装'
fi
"

# 脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo ""
echo ">>> 4. 配置 nginx..."

# 上传 nginx 配置到用户目录
eval $SCP_CMD "${SCRIPT_DIR}/nginx/fitness.conf" ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/fitness.conf

# 使用 sudo 配置 nginx (通过 -S 从 stdin 读取密码)
eval $SSH_CMD "
# 备份并创建 nginx 配置目录
echo 'JinYanru(()!)(' | sudo -S mkdir -p /etc/nginx/conf.d 2>/dev/null

# 复制配置文件
echo 'JinYanru(()!)(' | sudo -S cp ${REMOTE_DIR}/fitness.conf /etc/nginx/conf.d/fitness.conf 2>/dev/null

# 备份默认配置
echo 'JinYanru(()!)(' | sudo -S cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak 2>/dev/null || true

# 确保包含 conf.d 目录
if ! echo 'JinYanru(()!)(' | sudo -S grep -q 'include.*conf.d' /etc/nginx/nginx.conf 2>/dev/null; then
    echo 'JinYanru(()!)(' | sudo -S sed -i '/http {/a\\    include /etc/nginx/conf.d/*.conf;' /etc/nginx/nginx.conf 2>/dev/null
fi

# 测试 nginx 配置
echo 'JinYanru(()!)(' | sudo -S nginx -t 2>&1

# 重载 nginx
echo 'JinYanru(()!)(' | sudo -S systemctl reload nginx 2>/dev/null || echo 'JinYanru(()!)(' | sudo -S nginx -s reload 2>/dev/null || true
echo '✓ nginx 配置完成'
"

echo ""
echo ">>> 5. 修复目录权限（允许 nginx 访问）..."
eval $SSH_CMD "
chmod 755 /home/raza
echo '✓ 目录权限已修复'
"

echo ""
echo "=========================================="
echo "  初始化完成！"
echo "=========================================="
echo ""
echo "目录结构:"
echo "  ${REMOTE_DIR}/"
echo "  ├── dist/       前端静态文件"
echo "  ├── backend/    后端 jar 包"
echo "  ├── logs/       运行日志"
echo "  └── backup/     备份目录"
echo ""
echo "现在可以运行 ./deploy.sh 进行部署"
echo ""