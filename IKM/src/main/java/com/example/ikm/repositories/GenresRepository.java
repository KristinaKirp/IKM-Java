package com.example.ikm.repositories;

import com.example.ikm.entity.Genres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenresRepository extends JpaRepository<Genres, Long> {

    // Поиск жанра по названию
    Optional<Genres> findByName(String name);

    // Поиск жанров по названию (частичное совпадение)
    List<Genres> findByNameContainingIgnoreCase(String name);

    // Проверка существования жанра по названию
    boolean existsByName(String name);
}