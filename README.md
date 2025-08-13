# 1. Получить всех пользователей
curl http://localhost:8080/api/users

# 2. Создать нового пользователя
curl -X POST http://localhost:8080/api/users \
-H "Content-Type: application/json" \
-d '{"name":"Test User","email":"test@example.com","age":25}'

# 3. Открыть в новом терминале поток пользователей
curl http://localhost:8080/api/users/stream

# 4. Открыть в другом терминале поток RabbitMQ
curl http://localhost:8080/api/users/rabbit-stream

# 5. Обновить пользователя
curl -X PUT http://localhost:8080/api/users/4 \
-H "Content-Type: application/json" \
-d '{"name":"Updated User","email":"updated@example.com","age":30}'

# 6. Удалить пользователя
curl -X DELETE http://localhost:8080/api/users/4

# 7. Создать контейнер RabbitMQ в Docker
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 8. Запуск проекта в Spring boot
mvn spring-boot:run
