package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    User create(User user);

    User update(User user);

    User findById(int id);

    Collection<User> findAll();

    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    List<User> findFriends(int userId);

    List<User> findCommonFriends(int userId, int otherId);
}