@echo off
echo ========================================
echo Building workstation...
echo ========================================
cd /d D:\prodman\workstation
call mvn clean package -DskipTests
if errorlevel 1 goto error

echo.
echo ========================================
echo Stopping and removing workstation container...
echo ========================================
cd /d D:\prodman
docker-compose stop workstation
docker-compose rm -f workstation
docker rmi -f prodman-workstation

echo.
echo ========================================
echo Starting workstation with fresh build...
echo ========================================
docker-compose up -d --build workstation

echo.
echo ========================================
echo Done!
echo Check: http://localhost:8083/swagger-ui/index.html
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