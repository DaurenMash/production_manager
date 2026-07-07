@echo off
echo ========================================
echo Building test-service...
echo ========================================
cd /d D:\prodman\test-service
call mvn clean install -U
if errorlevel 1 goto error

echo.
echo ========================================
echo Stopping containers...
echo ========================================
cd /d D:\prodman
docker-compose down test-service

echo.
echo ========================================
echo Removing old images...
echo ========================================
docker rmi -f prodman-test-service
docker system prune -f

echo.
echo ========================================
echo Starting containers...
echo ========================================
docker-compose build --no-cache test-service
docker-compose up -d test-service

echo.
echo ========================================
echo Done!
echo Check: http://localhost:8085/swagger-ui/index.html
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