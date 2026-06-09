@echo off
echo ========================================
echo  新能源汽车充电排队与预约系统 - 启动脚本
echo ========================================
echo.

echo [1/4] 检查 Docker 是否运行...
docker version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker 未运行，请先启动 Docker Desktop
    pause
    exit /b 1
)
echo Docker 运行正常
echo.

echo [2/4] 停止旧容器...
docker-compose down
echo.

echo [3/4] 构建并启动所有服务...
docker-compose up -d --build
echo.

echo [4/4] 等待服务启动...
timeout /t 30 /nobreak >nul
echo.

echo ========================================
echo  服务启动完成！
echo ========================================
echo  前端地址: http://localhost
echo  后端地址: http://localhost:8080/api
echo  MySQL: localhost:3306 / charging_system
echo  Redis: localhost:6379
echo.
echo  查看日志: docker-compose logs -f
echo  停止服务: docker-compose down
echo ========================================
pause
