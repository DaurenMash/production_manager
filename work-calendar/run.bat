@echo off
echo ================================================
echo   Work Calendar Service
echo ================================================
echo.

:: Переходим в папку сервиса
cd /d D:\prodman\work-calendar

:: Останавливаем старые контейнеры
echo [1/4] Stopping existing containers...
docker-compose down 2>nul

:: Удаляем старый образ
echo [2/4] Removing old image...
docker rmi work-calendar 2>nul

:: Собираем проект
echo [3/4] Building project...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

:: Запускаем через Docker Compose
echo [4/4] Starting service...
docker-compose --env-file .env.dev up -d --build

echo.
echo ================================================
echo   Work Calendar Service is running!
echo ================================================
echo.
echo   Service:    http://localhost:8082
echo   Swagger UI: http://localhost:8082/swagger-ui.html
echo.
echo   Check status: docker ps
echo   View logs:    docker logs -f work-calendar-app
echo.
pause
