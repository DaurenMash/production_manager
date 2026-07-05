@echo off
cd /d D:\prodman\prodman-desktop

echo ========================================
echo Building ProdMan Desktop...
echo ========================================

call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo BUILD FAILED!
    pause
    exit /b %errorlevel%
)

echo.
echo ========================================
echo Running ProdMan Desktop...
echo ========================================

java -jar target\prodman-desktop-1.0.0.jar

pause


@REM @echo off
@REM cd /d D:\prodman\prodman-desktop
@REM @REM set API_URL=http://localhost:8081/api/v1
@REM call mvn clean compile exec:java
@REM pause