package com.example.ikm.repositories;

import com.example.ikm.entity.Books;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью Books в базе данных.
 * Наследует JpaRepository для получения стандартных CRUD операций.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Repository - помечает интерфейс как репозиторий Spring Data</li>
 * </ul>
 * </p>
 */
@Repository
public interface BooksRepository extends JpaRepository<Books, Long> {

    /**
     * Находит книги по частичному совпадению названия (без учета регистра).
     *
     * @param title часть названия для поиска
     * @return список книг с названиями, содержащими указанную строку
     */
    List<Books> findByTitleContainingIgnoreCase(String title);

    /**
     * Находит книги по году публикации.
     *
     * @param publishYear год публикации
     * @return список книг, опубликованных в указанный год
     */
    List<Books> findByPublishYear(Integer publishYear);

    /**
     * Находит книги по идентификатору автора.
     *
     * @param authorId идентификатор автора
     * @return список книг указанного автора
     */
    List<Books> findByAuthorId(Long authorId);

    /**
     * Находит книги по содержанию отзыва (без учета регистра).
     *
     * @param feedback текст для поиска в отзывах
     * @return список книг с отзывами, содержащими указанный текст
     */
    List<Books> findByFeedbackContainingIgnoreCase(String feedback);

    /**
     * Находит книги по частичному совпадению имени автора и названия книги (без учета регистра).
     *
     * @param authorFirstName часть имени автора для поиска
     * @param title часть названия книги для поиска
     * @return список книг, удовлетворяющих обоим критериям
     */
    List<Books> findByAuthorFirstNameContainingIgnoreCaseAndTitleContainingIgnoreCase(
            String authorFirstName, String title);

}