package com.example.ikm.repositories;

import com.example.ikm.entity.Authors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * Репозиторий для работы с сущностью Authors в базе данных.
 * Наследует JpaRepository для получения стандартных CRUD операций.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Repository - помечает интерфейс как репозиторий Spring Data</li>
 * </ul>
 * </p>
 *
 * <p>Методы репозитория используют соглашение об именовании Spring Data JPA,
 * что позволяет автоматически генерировать SQL-запросы на основе имен методов.</p>
 */
@Repository
public interface AuthorsRepository extends JpaRepository<Authors, Long> {
    /**
     * Находит авторов по частичному совпадению имени (без учета регистра).
     *
     * @param firstName часть имени для поиска
     * @return список авторов с именами, содержащими указанную строку
     */
    List<Authors> findByFirstNameContainingIgnoreCase(String firstName);
    /**
     * Находит авторов по частичному совпадению фамилии (без учета регистра).
     *
     * @param lastName часть фамилии для поиска
     * @return список авторов с фамилиями, содержащими указанную строку
     */
    List<Authors> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Находит авторов по году рождения.
     *
     * @param birthYear год рождения
     * @return список авторов, родившихся в указанный год
     */
    List<Authors> findByBirthYear(Integer birthYear);

    /**
     * Находит авторов, родившихся в указанном диапазоне лет.
     *
     * @param startYear начальный год диапазона
     * @param endYear конечный год диапазона
     * @return список авторов, родившихся в указанном диапазоне
     */
    List<Authors> findByBirthYearBetween(Integer startYear, Integer endYear);

    /**
     * Находит авторов по частичному совпадению имени или фамилии (без учета регистра).
     *
     * @param firstName часть имени для поиска
     * @param lastName часть фамилии для поиска
     * @return список авторов с именами или фамилиями, содержащими указанные строки
     */
    List<Authors> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    /**
     * Находит автора по точному совпадению имени и фамилии (без учета регистра).
     *
     * @param firstName имя автора
     * @param lastName фамилия автора
     * @return Optional с автором, если найден
     */
    Optional<Authors> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}