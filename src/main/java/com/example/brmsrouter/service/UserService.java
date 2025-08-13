package com.example.brmsrouter.service;


import com.example.brmsrouter.entity.User;
import com.example.brmsrouter.repository.UserMockRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserMockRepository userRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final Random random = new Random();

    // CREATE - создание пользователя с отправкой в RabbitMQ
    public Mono<User> createUser(User user) {
        return Mono.fromCallable(() -> {
            User savedUser = userRepository.save(user);
            // Отправляем в RabbitMQ
            sendToRabbitMQ(savedUser);
            return savedUser;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // READ - получение всех пользователей как поток
    public Flux<User> getAllUsers() {
        return Mono.fromCallable(userRepository::findAll)
                .flatMapIterable(users -> users)
                .subscribeOn(Schedulers.boundedElastic());
    }

    // READ - получение пользователя по ID
    public Mono<User> getUserById(Long id) {
        return Mono.fromCallable(() -> userRepository.findById(id))
                .filter(Optional -> Optional.isPresent())
                .map(Optional -> Optional.get())
                .subscribeOn(Schedulers.boundedElastic());
    }

    // UPDATE - обновление пользователя
    public Mono<User> updateUser(Long id, User userDetails) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setAge(userDetails.getAge());
            User updatedUser = userRepository.save(user);

            // Отправляем обновленного пользователя в RabbitMQ
            sendToRabbitMQ(updatedUser);
            return updatedUser;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // DELETE - удаление пользователя
    public Mono<Void> deleteUser(Long id) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            boolean deleted = userRepository.deleteById(id);
            if (!deleted) {
                throw new RuntimeException("Failed to delete user with id: " + id);
            }

            // Отправляем информацию об удалении в RabbitMQ
            String message = "User deleted: " + user.toString();
            rabbitTemplate.convertAndSend("user.exchange", "user.deleted", message);
            System.out.println("Sent to RabbitMQ: " + message);

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // Поток случайных пользователей (имитация реального времени)
    public Flux<User> randomUserStream() {
        String[] names = {"Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry"};
        String[] domains = {"example.com", "test.com", "demo.com", "mock.com"};

        return Flux.interval(Duration.ofSeconds(3))
                .publishOn(Schedulers.parallel())
                .map(i -> {
                    String name = names[random.nextInt(names.length)];
                    String domain = domains[random.nextInt(domains.length)];
                    int age = random.nextInt(50) + 18;

                    User user = new User(
                            name + "_" + System.currentTimeMillis(),
                            name.toLowerCase() + "@" + domain,
                            age
                    );

                    User savedUser = userRepository.save(user);
                    sendToRabbitMQ(savedUser);
                    return savedUser;
                });
    }

    // Поток из RabbitMQ (для демонстрации)
    public Flux<String> getRabbitMQStream() {
        return Flux.interval(Duration.ofSeconds(4))
                .map(i -> "RabbitMQ message #" + i + " - " + System.currentTimeMillis())
                .publishOn(Schedulers.parallel());
    }

    // Отправка в RabbitMQ
    private void sendToRabbitMQ(User user) {
        try {
            rabbitTemplate.convertAndSend("user.exchange", "user.created", user.toString());
            System.out.println("Sent to RabbitMQ: " + user.toString());
        } catch (Exception e) {
            System.err.println("Failed to send to RabbitMQ: " + e.getMessage());
        }
    }
}