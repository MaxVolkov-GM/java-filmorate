package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserValidationTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = mock(UserStorage.class);
        when(userStorage.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        userService = new UserService(userStorage);
    }

    @Test
    void shouldThrowWhenEmailIsBlank() {
        User user = new User();
        user.setEmail(" ");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
    }

    @Test
    void shouldThrowWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("email@mail.com");
        user.setLogin(" ");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
    }

    @Test
    void shouldThrowWhenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("email@mail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
    }

    @Test
    void shouldSetNameIfBlank() {
        User user = new User();
        user.setEmail("email@mail.com");
        user.setLogin("login");
        user.setName(" ");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertDoesNotThrow(() -> userService.createUser(user));
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setEmail("email@mail.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertDoesNotThrow(() -> userService.createUser(user));
    }
}