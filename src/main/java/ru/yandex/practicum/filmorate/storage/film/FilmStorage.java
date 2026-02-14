package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Film findById(int id);

    Collection<Film> findAll();

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> findPopular(int count);
}