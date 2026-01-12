package com.example.ikm.service;

import com.example.ikm.entity.Authors;
import com.example.ikm.repositories.AuthorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
/**
 * Сервисный класс для работы с авторами.
 * Содержит бизнес-логику операций с авторами.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Service - помечает класс как сервисный компонент Spring</li>
 * </ul>
 * </p>
 */
@Service
public class AuthorsService {
    private final AuthorsRepository authorRepository;
    /**
     * Конструктор с внедрением зависимости репозитория.
     *
     * @param authorRepository репозиторий для работы с авторами в БД
     */
    @Autowired
    public AuthorsService(AuthorsRepository authorRepository) {
        this.authorRepository = authorRepository;
    }
    /**
     * Получает список всех авторов.
     *
     * @return список всех авторов
     */
    public List<Authors> getAllAuthors() {
        return authorRepository.findAll();
    }
    /**
     * Находит автора по идентификатору.
     *
     * @param id идентификатор автора
     * @return Optional с автором, если найден
     */
    public Optional<Authors> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }
    /**
     * Сохраняет нового автора или обновляет существующего.
     *
     * @param author объект автора для сохранения
     * @return сохраненный автор
     */
    public Authors saveAuthor(Authors author) {
        return authorRepository.save(author);
    }
    /**
     * Обновляет данные существующего автора.
     *
     * @param id идентификатор автора для обновления
     * @param authorDetails новые данные автора
     * @return обновленный автор
     * @throws RuntimeException если автор не найден
     */
    public Authors updateAuthor(Long id, Authors authorDetails) {
        Authors author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));

        author.setFirstName(authorDetails.getFirstName());
        author.setLastName(authorDetails.getLastName());
        author.setBirthYear(authorDetails.getBirthYear());

        return authorRepository.save(author);
    }
    /**
     * Удаляет автора по идентификатору.
     *
     * @param id идентификатор автора для удаления
     */
    public void deleteAuthor(Long id) {
        Authors author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));
        if (author.getBooks() != null) {
            author.getBooks().size();
        }

        authorRepository.delete(author);
    }
    /**
     * Выполняет поиск авторов по различным критериям.
     *
     * @param searchType тип поиска (firstName, lastName, birthYear, fullName)
     * @param searchQuery поисковый запрос
     * @return список авторов, соответствующих критериям поиска
     */
    public List<Authors> searchAuthors(String searchType, String searchQuery) {
        List<Authors> authors;

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            authors = authorRepository.findAll();
        } else {
            switch (searchType != null ? searchType : "lastName") {
                case "firstName":
                    authors = authorRepository.findByFirstNameContainingIgnoreCase(searchQuery);
                    break;
                case "birthYear":
                    try {
                        Integer year = Integer.parseInt(searchQuery);
                        authors = authorRepository.findByBirthYear(year);
                    } catch (NumberFormatException e) {
                        authors = List.of();
                    }
                    break;
                case "fullName":
                    authors = authorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                            searchQuery, searchQuery);
                    break;
                default:
                    authors = authorRepository.findByLastNameContainingIgnoreCase(searchQuery);
                    break;
            }
        }

        return authors;
    }
    /**
     * Ищет авторов по диапазону годов рождения.
     *
     * @param startYear начальный год диапазона
     * @param endYear конечный год диапазона
     * @return список авторов, родившихся в указанном диапазоне
     */
    public List<Authors> searchAuthorsByYearRange(Integer startYear, Integer endYear) {
        if (startYear != null && endYear != null) {
            return authorRepository.findByBirthYearBetween(startYear, endYear);
        } else if (startYear != null) {
            return authorRepository.findByBirthYear(startYear);
        } else {
            return authorRepository.findAll();
        }
    }
    /**
     * Подсчитывает общее количество авторов.
     *
     * @return количество авторов
     */
    public long countAuthors() {
        return authorRepository.count();
    }
    /**
     * Находит существующего автора или создает нового, если не найден.
     *
     * @param firstName имя автора
     * @param lastName фамилия автора
     * @return существующий или созданный автор
     * @throws IllegalArgumentException если имя или фамилия пустые
     */
    public Authors findOrCreateAuthor(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя и фамилия автора обязательны");
        }

        String cleanFirstName = firstName.trim();
        String cleanLastName = lastName.trim();

        return authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(cleanFirstName, cleanLastName)
                .orElseGet(() -> {
                    Authors newAuthor = new Authors();
                    newAuthor.setFirstName(cleanFirstName);
                    newAuthor.setLastName(cleanLastName);
                    return authorRepository.save(newAuthor);
                });
    }
    public boolean authorExists(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return false;
        }
        return authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                firstName.trim(), lastName.trim()).isPresent();
    }
}