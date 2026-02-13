package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User update(User user) {
        findById(user.getId());

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, id);

        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return users.get(0);
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT id, email, login, name, birthday FROM users ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        });
    }

    @Override
    public void addFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);

        String sql = "MERGE INTO friends (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);

        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> findFriends(int userId) {
        findById(userId);

        String sql = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friends f
                JOIN users u ON u.id = f.friend_id
                WHERE f.user_id = ?
                ORDER BY u.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId);
    }

    @Override
    public List<User> findCommonFriends(int userId, int otherId) {
        findById(userId);
        findById(otherId);

        String sql = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friends f1
                JOIN friends f2 ON f1.friend_id = f2.friend_id
                JOIN users u ON u.id = f1.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                ORDER BY u.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId, otherId);
    }
}