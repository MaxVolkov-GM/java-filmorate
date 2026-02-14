package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User createUser(User user) {
        validateUser(user);
        normalizeUserName(user);
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        validateUser(user);
        normalizeUserName(user);
        return userStorage.update(user);
    }

    public Collection<User> getUsers() {
        return userStorage.findAll();
    }

    public User getUserById(int id) {
        return userStorage.findById(id);
    }

    public void addFriend(int userId, int friendId) {
        userStorage.findById(userId);
        userStorage.findById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        userStorage.findById(userId);
        userStorage.findById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        userStorage.findById(userId);
        return userStorage.findFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        userStorage.findById(userId);
        userStorage.findById(otherId);
        return userStorage.findCommonFriends(userId, otherId);
    }

    private void normalizeUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Некорректный email");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new IllegalArgumentException("Некорректный login");
        }
        if (user.getLogin().contains(" ")) {
            throw new IllegalArgumentException("Некорректный login");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
    }
}