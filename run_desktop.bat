@echo off
cd /d D:\prodman\prodman-desktop
@REM set API_URL=http://localhost:8081/api/v1
call mvn clean compile exec:java
pause