@echo off
setlocal

set SERVICE_NAME=employee-service
set IMAGE_NAME=prodman-employee-service
set CONTAINER_NAME=employee-service-app
set PROJECT_DIR=D:\prodman\employee-service
set ROOT_DIR=D:\prodman

echo ========================================
echo Building %SERVICE_NAME%...
echo ========================================
cd /d %PROJECT_DIR%
call mvn clean package -DskipTests
if errorlevel 1 goto error

echo.
echo ========================================
echo Stopping and removing container...
echo ========================================
docker stop %CONTAINER_NAME% 2>nul
docker rm %CONTAINER_NAME% 2>nul

echo.
echo ========================================
echo Removing old image...
echo ========================================
docker rmi -f %IMAGE_NAME% 2>nul

echo.
echo ========================================
echo Rebuilding image (no cache)...
echo ========================================
cd /d %PROJECT_DIR%
set DOCKER_BUILDKIT=0
docker build --no-cache -t %IMAGE_NAME% .
if errorlevel 1 goto error

echo.
echo ========================================
echo Starting container via docker-compose...
echo ========================================
cd /d %ROOT_DIR%
docker-compose up -d %SERVICE_NAME%

echo.
echo ========================================
echo Done! Waiting for startup (10 sec)...
echo Check: http://localhost:8084/swagger-ui/index.html
echo ========================================
timeout /t 10 /nobreak >nul
docker logs %CONTAINER_NAME% --tail 15

pause
exit /b 0

:error
echo.
echo ========================================
echo BUILD FAILED!
echo ========================================
pause
exit /b 1