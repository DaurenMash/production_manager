@echo off
echo ========================================
echo Building user-service...
echo ========================================
cd /d D:\prodman\user-service
call mvn clean install
if errorlevel 1 goto error

echo.
echo ========================================
echo Stopping containers...
echo ========================================
cd /d D:\prodman
docker-compose down

echo.
echo ========================================
echo Removing old images...
echo ========================================

docker rmi -f prodman-user-service
docker system prune -f

echo.
echo ========================================
echo Starting containers...
echo ========================================
docker-compose build --no-cache user-service
docker-compose up -d

echo.
echo ========================================
echo Done!
echo Check: http://localhost:8081/swagger-ui/index.html
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