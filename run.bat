@echo off
echo ========================================
echo Building user-service...
echo ========================================
cd /d D:\prodman\user-service
call mvn clean install
if errorlevel 1 goto error

echo.
echo ========================================
echo Building work-calendar...
echo ========================================
cd /d D:\prodman\work-calendar
call mvn clean install
if errorlevel 1 goto error

echo.
echo ========================================
echo Building api-gateway...
echo ========================================
cd /d D:\prodman\prodman-api-gateway
call mvn clean install
if errorlevel 1 goto error

echo.
echo ========================================
echo Building workstation...
echo ========================================
cd /d D:\prodman\workstation
call mvn clean install
if errorlevel 1 goto error

echo.
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
docker-compose down

echo.
echo ========================================
echo Removing old images...
echo ========================================
docker rmi -f prodman-user-service
docker rmi -f prodman-work-calendar
docker rmi -f prodman-api-gateway
docker rmi -f prodman-workstation
docker rmi -f prodman-employee-service
docker system prune -f

echo.
echo ========================================
echo Starting containers...
echo ========================================
docker-compose up -d --build

echo.
echo ========================================
echo Done!
echo Check: http://localhost:8888/swagger-ui/index.html
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