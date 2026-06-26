echo ========================================
echo Building api-gateway...
echo ========================================
cd /d D:\prodman\prodman-api-gateway
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

docker rmi -f prodman-api-gateway
docker system prune -f

echo.
echo ========================================
echo Starting containers...
echo ========================================
docker-compose build --no-cache api-gateway
docker-compose up -d

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