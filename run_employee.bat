@echo off
echo ========================================
echo Building employee-service...
echo ========================================
cd /d D:\prodman\employee-service
call mvn clean install
if errorlevel 1 goto error

echo.
echo ========================================
echo Stopping containers...
echo ========================================
cd /d D:\prodman
docker-compose down employee-service

echo.
echo ========================================
echo Removing old images...
echo ========================================
docker rmi -f prodman-employee-service
docker system prune -f

echo.
echo ========================================
echo Starting containers...
echo ========================================
docker-compose build --no-cache employee-service
docker-compose up -d employee-service

echo.
echo ========================================
echo Done!
echo Check: http://localhost:8084/swagger-ui/index.html
echo ========================================
pause
exit /b 0

:error
echo.
echo ========================================
echo BUILD FAILED!
echo ========================================
pause
exit /b 1