docker-compose down
docker rmi user-service 2>nul
call mvn clean package -DskipTests
docker-compose --env-file .env.dev up -d --build

@REM http://localhost:8080/swagger-ui.html