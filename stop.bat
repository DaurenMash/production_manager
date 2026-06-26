@echo off
echo ================================================
echo   Stopping all ProdMan services
echo ================================================
echo.

cd /d D:\prodman

echo Stopping all containers...
docker-compose down --remove-orphans

echo.
echo All services stopped!
pause