package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        Integer mpaId = film.getMpa() == null ? null : film.getMpa().getId();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (mpaId == null) {
                ps.setObject(5, null);
            } else {
                ps.setInt(5, mpaId);
            }
            return ps;
        }, keyHolder);

        int filmId = keyHolder.getKey().intValue();
        film.setId(filmId);

        updateFilmGenres(filmId, film.getGenres());
        return findById(filmId);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        Integer mpaId = film.getMpa() == null ? null : film.getMpa().getId();

        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId,
                film.getId()
        );

        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        updateFilmGenres(film.getId(), film.getGenres());
        return findById(film.getId());
    }

    @Override
    public Film findById(int id) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                Mpa mpa = new Mpa();
                mpa.setId(mpaId);
                mpa.setName(rs.getString("mpa_name"));
                film.setMpa(mpa);
            }

            film.setGenres(loadGenresForFilm(id));
            return film;
        }, id);

        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return films.get(0);
    }

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                ORDER BY f.id
                """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            int filmId = rs.getInt("id");

            film.setId(filmId);
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                Mpa mpa = new Mpa();
                mpa.setId(mpaId);
                mpa.setName(rs.getString("mpa_name"));
                film.setMpa(mpa);
            }

            film.setGenres(loadGenresForFilm(filmId));
            return film;
        });

        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> findPopular(int count) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.name AS mpa_name,
                       COUNT(l.user_id) AS likes_count
                FROM films f
                LEFT JOIN likes l ON f.id = l.film_id
                LEFT JOIN mpa m ON f.mpa_id = m.id
                GROUP BY f.id, m.id, m.name
                ORDER BY likes_count DESC, f.id ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            int filmId = rs.getInt("id");

            film.setId(filmId);
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                Mpa mpa = new Mpa();
                mpa.setId(mpaId);
                mpa.setName(rs.getString("mpa_name"));
                film.setMpa(mpa);
            }

            film.setGenres(loadGenresForFilm(filmId));
            return film;
        }, count);
    }

    private void updateFilmGenres(int filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);

        if (genres == null || genres.isEmpty()) {
            return;
        }

        Set<Integer> uniqueGenreIds = new LinkedHashSet<>();
        for (Genre g : genres) {
            if (g != null) {
                uniqueGenreIds.add(g.getId());
            }
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Integer genreId : uniqueGenreIds) {
            jdbcTemplate.update(sql, filmId, genreId);
        }
    }

    private Set<Genre> loadGenresForFilm(int filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);

        return new LinkedHashSet<>(genres);
    }
}