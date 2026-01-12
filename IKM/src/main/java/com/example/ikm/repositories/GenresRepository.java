package com.example.ikm.repositories;

import com.example.ikm.entity.Genres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Genres в базе данных.
 * Наследует JpaRepository для получения стандартных CRUD операций.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Repository - помечает интерфейс как репозиторий Spring Data</li>
 * </ul>
 * </p>
 */
@Repository
public interface GenresRepository extends JpaRepository<Genres, Long> {

    /**
     * Находит жанр по точному названию.
     *
     * @param name название жанра
     * @return Optional с жанром, если найден
     */
    Optional<Genres> findByName(String name);

    /**
     * Находит жанры по частичному совпадению названия (без учета регистра).
     *
     * @param name часть названия для поиска
     * @return список жанров с названиями, содержащими указанную строку
     */
    List<Genres> findByNameContainingIgnoreCase(String name);

    /**
     * Проверяет существование жанра по названию.
     *
     * @param name название жанра для проверки
     * @return true, если жанр существует, иначе false
     */
    boolean existsByName(String name);
}