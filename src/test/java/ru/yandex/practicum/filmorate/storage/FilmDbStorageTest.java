package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    void createAndFindByIdShouldReturnFilmWithMpaAndGenres() {
        Film film = new Film();
        film.setName("Film A");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre g1 = new Genre();
        g1.setId(1);
        Genre g2 = new Genre();
        g2.setId(2);
        film.setGenres(Set.of(g1, g2));

        Film created = filmDbStorage.create(film);
        Film fromDb = filmDbStorage.findById(created.getId());

        assertThat(fromDb.getId()).isPositive();
        assertThat(fromDb.getMpa()).isNotNull();
        assertThat(fromDb.getMpa().getId()).isEqualTo(1);
        assertThat(fromDb.getMpa().getName()).isNotBlank();

        assertThat(fromDb.getGenres()).isNotNull();
        assertThat(fromDb.getGenres()).hasSize(2);
        assertThat(fromDb.getGenres().stream().map(Genre::getId).toList())
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void updateShouldReplaceGenres() {
        Film film = new Film();
        film.setName("Film B");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2001, 1, 1));
        film.setDuration(90);

        Mpa mpa = new Mpa();
        mpa.setId(2);
        film.setMpa(mpa);

        Genre g1 = new Genre();
        g1.setId(1);
        film.setGenres(Set.of(g1));

        Film created = filmDbStorage.create(film);

        Genre g2 = new Genre();
        g2.setId(3);
        Genre g3 = new Genre();
        g3.setId(4);

        created.setName("Film B Updated");
        created.setGenres(Set.of(g2, g3));

        filmDbStorage.update(created);
        Film fromDb = filmDbStorage.findById(created.getId());

        assertThat(fromDb.getName()).isEqualTo("Film B Updated");
        assertThat(fromDb.getGenres().stream().map(Genre::getId).toList())
                .containsExactlyInAnyOrder(3, 4);
    }

    @Test
    void findAllShouldReturnCreatedFilms() {
        Film f1 = new Film();
        f1.setName("Film 1");
        f1.setDescription("D");
        f1.setReleaseDate(LocalDate.of(2010, 1, 1));
        f1.setDuration(100);
        f1 = filmDbStorage.create(f1);

        Film f2 = new Film();
        f2.setName("Film 2");
        f2.setDescription("D");
        f2.setReleaseDate(LocalDate.of(2011, 1, 1));
        f2.setDuration(110);
        f2 = filmDbStorage.create(f2);

        var all = filmDbStorage.findAll();
        assertThat(all).extracting(Film::getId).contains(f1.getId(), f2.getId());
    }

    @Test
    void likesShouldAffectPopularAndRemoveLikeShouldReorder() {
        User u1 = new User();
        u1.setEmail("u1@mail.ru");
        u1.setLogin("u1");
        u1.setName("u1");
        u1.setBirthday(LocalDate.of(1990, 1, 1));
        u1 = userDbStorage.create(u1);

        User u2 = new User();
        u2.setEmail("u2@mail.ru");
        u2.setLogin("u2");
        u2.setName("u2");
        u2.setBirthday(LocalDate.of(1991, 1, 1));
        u2 = userDbStorage.create(u2);

        Film f1 = new Film();
        f1.setName("Film 1");
        f1.setDescription("D");
        f1.setReleaseDate(LocalDate.of(2010, 1, 1));
        f1.setDuration(100);
        f1 = filmDbStorage.create(f1);

        Film f2 = new Film();
        f2.setName("Film 2");
        f2.setDescription("D");
        f2.setReleaseDate(LocalDate.of(2011, 1, 1));
        f2.setDuration(110);
        f2 = filmDbStorage.create(f2);

        filmDbStorage.addLike(f1.getId(), u1.getId());

        filmDbStorage.addLike(f2.getId(), u1.getId());
        filmDbStorage.addLike(f2.getId(), u2.getId());

        var popularTop1 = filmDbStorage.findPopular(1);
        assertThat(popularTop1).hasSize(1);
        assertThat(popularTop1.get(0).getId()).isEqualTo(f2.getId());

        filmDbStorage.removeLike(f2.getId(), u2.getId());

        var popularAfterRemove = filmDbStorage.findPopular(1);
        assertThat(popularAfterRemove).hasSize(1);
        assertThat(popularAfterRemove.get(0).getId()).isEqualTo(f1.getId());
    }
}