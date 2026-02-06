package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private ValidatorFactory factory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = validFilm();
        film.setName("   ");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDescriptionMoreThan200() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDurationNotPositive() {
        Film film = validFilm();
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassOnBoundaryDescription200() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenReleaseDateBeforeCinemaBirthday() {
        FilmController controller = new FilmController();

        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1890, 3, 25));

        assertThrows(ValidationException.class, () -> controller.createFilm(film));
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(120);
        return film;
    }
}