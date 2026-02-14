package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Test
    void findAllShouldReturnGenres() {
        var genres = genreDbStorage.findAll();
        assertThat(genres).isNotNull();
        assertThat(genres.size()).isGreaterThanOrEqualTo(6);
        assertThat(genres.get(0).getId()).isEqualTo(1);
    }

    @Test
    void findByIdShouldReturnGenre() {
        var genre = genreDbStorage.findById(1);
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isNotBlank();
    }

    @Test
    void findByIdShouldThrowWhenNotFound() {
        assertThrows(NotFoundException.class, () -> genreDbStorage.findById(999));
    }
}