package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class FilmValidationTest {

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = mock(FilmStorage.class);
        UserStorage userStorage = mock(UserStorage.class);
        filmService = new FilmService(filmStorage, userStorage);
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        Film film = new Film();
        film.setName(" ");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(IllegalArgumentException.class, () -> filmService.createFilm(film));
    }

    @Test
    void shouldThrowWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(IllegalArgumentException.class, () -> filmService.createFilm(film));
    }

    @Test
    void shouldThrowWhenReleaseDateBeforeCinemaBirthday() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(100);

        assertThrows(IllegalArgumentException.class, () -> filmService.createFilm(film));
    }

    @Test
    void shouldThrowWhenDurationIsNotPositive() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        assertThrows(IllegalArgumentException.class, () -> filmService.createFilm(film));
    }

    @Test
    void shouldThrowWhenFilmIsNull() {
        assertThrows(IllegalArgumentException.class, () -> filmService.createFilm(null));
    }

    @Test
    void shouldNotThrowWhenFilmIsValid() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertDoesNotThrow(() -> filmService.createFilm(film));
    }
}