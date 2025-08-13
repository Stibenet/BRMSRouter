package com.example.brmsrouter.repository;

import com.example.brmsrouter.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserMockRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserMockRepository() {
        // Инициализация тестовых данных
        save(new User("Alice", "alice@example.com", 25));
        save(new User("Bob", "bob@example.com", 30));
        save(new User("Charlie", "charlie@example.com", 35));
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.put(user.getId(), user);
        return user;
    }

    public List<User> findAll() {
        return users.values().stream().toList();
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public boolean deleteById(Long id) {
        return users.remove(id) != null;
    }

    public long count() {
        return users.size();
    }
}