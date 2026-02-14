package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Film {
    private int id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private LocalDate releaseDate;

    @Positive
    private int duration;

    private Mpa mpa;

    private Set<Genre> genres = new LinkedHashSet<>();
}