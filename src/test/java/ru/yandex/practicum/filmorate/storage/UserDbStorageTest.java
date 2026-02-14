package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    void createAndFindByIdShouldReturnUser() {
        User user = new User();
        user.setEmail("u@mail.ru");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userDbStorage.create(user);
        User fromDb = userDbStorage.findById(created.getId());

        assertThat(fromDb.getId()).isPositive();
        assertThat(fromDb.getEmail()).isEqualTo("u@mail.ru");
        assertThat(fromDb.getLogin()).isEqualTo("login");
        assertThat(fromDb.getName()).isEqualTo("Name");
    }

    @Test
    void updateShouldUpdateUser() {
        User user = new User();
        user.setEmail("u1@mail.ru");
        user.setLogin("u1");
        user.setName("u1");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userDbStorage.create(user);

        created.setEmail("u1_new@mail.ru");
        created.setName("new_name");

        userDbStorage.update(created);
        User fromDb = userDbStorage.findById(created.getId());

        assertThat(fromDb.getEmail()).isEqualTo("u1_new@mail.ru");
        assertThat(fromDb.getName()).isEqualTo("new_name");
    }

    @Test
    void findAllShouldReturnAllUsers() {
        User u1 = new User();
        u1.setEmail("a@mail.ru");
        u1.setLogin("a");
        u1.setName("a");
        u1.setBirthday(LocalDate.of(1990, 1, 1));
        userDbStorage.create(u1);

        User u2 = new User();
        u2.setEmail("b@mail.ru");
        u2.setLogin("b");
        u2.setName("b");
        u2.setBirthday(LocalDate.of(1991, 1, 1));
        userDbStorage.create(u2);

        var all = userDbStorage.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void addAndRemoveFriendShouldAffectFriendsListOneWay() {
        User a = new User();
        a.setEmail("a@mail.ru");
        a.setLogin("a");
        a.setName("a");
        a.setBirthday(LocalDate.of(1990, 1, 1));
        a = userDbStorage.create(a);

        User b = new User();
        b.setEmail("b@mail.ru");
        b.setLogin("b");
        b.setName("b");
        b.setBirthday(LocalDate.of(1991, 1, 1));
        b = userDbStorage.create(b);

        userDbStorage.addFriend(a.getId(), b.getId());

        var aFriends = userDbStorage.findFriends(a.getId());
        var bFriends = userDbStorage.findFriends(b.getId());

        assertThat(aFriends).extracting(User::getId).contains(b.getId());
        assertThat(bFriends).extracting(User::getId).doesNotContain(a.getId());

        userDbStorage.removeFriend(a.getId(), b.getId());
        var aFriendsAfter = userDbStorage.findFriends(a.getId());
        assertThat(aFriendsAfter).extracting(User::getId).doesNotContain(b.getId());
    }

    @Test
    void findCommonFriendsShouldReturnIntersection() {
        User a = new User();
        a.setEmail("a2@mail.ru");
        a.setLogin("a2");
        a.setName("a2");
        a.setBirthday(LocalDate.of(1990, 1, 1));
        a = userDbStorage.create(a);

        User d = new User();
        d.setEmail("d@mail.ru");
        d.setLogin("d");
        d.setName("d");
        d.setBirthday(LocalDate.of(1992, 1, 1));
        d = userDbStorage.create(d);

        User b = new User();
        b.setEmail("b2@mail.ru");
        b.setLogin("b2");
        b.setName("b2");
        b.setBirthday(LocalDate.of(1991, 1, 1));
        b = userDbStorage.create(b);

        User c = new User();
        c.setEmail("c@mail.ru");
        c.setLogin("c");
        c.setName("c");
        c.setBirthday(LocalDate.of(1993, 1, 1));
        c = userDbStorage.create(c);

        userDbStorage.addFriend(a.getId(), b.getId());
        userDbStorage.addFriend(a.getId(), c.getId());
        userDbStorage.addFriend(d.getId(), b.getId());

        var common = userDbStorage.findCommonFriends(a.getId(), d.getId());
        assertThat(common).hasSize(1);
        assertThat(common.get(0).getId()).isEqualTo(b.getId());
    }
}