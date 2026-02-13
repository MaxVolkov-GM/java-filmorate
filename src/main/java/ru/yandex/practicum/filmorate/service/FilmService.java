package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film createFilm(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        filmStorage.findById(film.getId());
        return filmStorage.update(film);
    }

    public Collection<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film getFilmById(int id) {
        return filmStorage.findById(id);
    }

    public void addLike(int filmId, int userId) {
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findPopular(count);
    }

    private void validateFilm(Film film) {
        if (film == null) {
            throw new IllegalArgumentException("Фильм не может быть null");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            throw new IllegalArgumentException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new IllegalArgumentException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() == null) {
            throw new IllegalArgumentException("Дата релиза не может быть null");
        }
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new IllegalArgumentException("Дата релиза не может быть раньше 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            throw new IllegalArgumentException("Продолжительность фильма должна быть положительной");
        }
        if (film.getMpa() != null && film.getMpa().getId() <= 0) {
            throw new NotFoundException("Некорректный рейтинг MPA");
        }
        if (film.getGenres() != null && film.getGenres().stream().anyMatch(g -> g.getId() <= 0)) {
            throw new NotFoundException("Некорректный жанр");
        }
    }
}