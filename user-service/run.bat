docker-compose down
docker rmi user-service 2>nul
call mvn clean package -DskipTests
docker-compose --env-file .env.dev up -d --build


echo Waiting for database to be ready...
timeout /t 10 /nobreak >nul

echo Creating users table...
docker exec -i user-service-postgres psql -U postgres -d prodman_users -c "
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
"

docker-compose restart user-service
docker logs -f user-service-app

echo.
echo ========================================
echo   Done!
echo ========================================
echo.
echo Check status: docker ps
echo Check logs:   docker logs -f user-service-app
echo Swagger UI:   http://localhost:8081/swagger-ui.html