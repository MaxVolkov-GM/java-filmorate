package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Test
    void findAllShouldReturnFiveMpa() {
        var mpas = mpaDbStorage.findAll();
        assertThat(mpas).hasSize(5);
        assertThat(mpas.get(0).getId()).isEqualTo(1);
    }

    @Test
    void findByIdShouldReturnMpa() {
        var mpa = mpaDbStorage.findById(1);
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isNotBlank();
    }

    @Test
    void findByIdShouldThrowWhenNotFound() {
        assertThrows(NotFoundException.class, () -> mpaDbStorage.findById(999));
    }
}