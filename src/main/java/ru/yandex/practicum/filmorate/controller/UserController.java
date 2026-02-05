package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        validateLoginNoSpaces(user.getLogin());

        user.setId(nextId++);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            log.warn("Ошибка обновления пользователя: id = null");
            throw new ValidationException("Id пользователя должен быть указан");
        }

        if (!users.containsKey(user.getId())) {
            log.warn("Попытка обновить несуществующего пользователя с id={}", user.getId());
            throw new ValidationException("Пользователь с таким id не найден");
        }

        validateLoginNoSpaces(user.getLogin());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    private void validateLoginNoSpaces(String login) {
        if (login != null && login.contains(" ")) {
            log.warn("Ошибка валидации пользователя: login содержит пробелы: '{}'", login);
            throw new ValidationException("Логин не должен содержать пробелы");
        }
    }
}
