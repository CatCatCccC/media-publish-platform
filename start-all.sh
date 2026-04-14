#!/bin/bash

echo "=========================================="
echo "  自媒体文章多平台发布系统启动脚本"
echo "=========================================="
echo ""

mkdir -p /home/coze/publish/uploads

echo "[1/3] Starting Backend (Spring Boot)..."
echo "      Backend: http://localhost:8080"
echo "      H2 Console: http://localhost:8080/h2-console"
echo ""
cd /home/coze/publish/backend
mvn spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!
echo "      Backend PID: $BACKEND_PID"

echo ""
echo "[2/3] Waiting for backend to start (10 seconds)..."
sleep 10

echo ""
echo "[3/3] Starting Frontend (Vue 3)..."
echo "      Frontend: http://localhost:5173"
echo ""
cd /home/coze/publish/frontend
npm run dev > frontend.log 2>&1 &
FRONTEND_PID=$!
echo "      Frontend PID: $FRONTEND_PID"

echo ""
echo "=========================================="
echo "  系统启动完成！"
echo "=========================================="
echo ""
echo "  后端地址: http://localhost:8080"
echo "  前端地址: http://localhost:5173"
echo "  H2控制台: http://localhost:8080/h2-console"
echo ""
echo "  日志文件:"
echo "    - Backend: /home/coze/publish/backend/backend.log"
echo "    - Frontend: /home/coze/publish/frontend/frontend.log"
echo ""
echo "  停止服务:"
echo "    kill $BACKEND_PID $FRONTEND_PID"
echo "=========================================="
