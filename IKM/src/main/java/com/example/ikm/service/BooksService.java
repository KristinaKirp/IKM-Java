package com.example.ikm.service;

import com.example.ikm.entity.Authors;
import com.example.ikm.entity.Books;
import com.example.ikm.entity.Genres;
import com.example.ikm.repositories.AuthorsRepository;
import com.example.ikm.repositories.BooksRepository;
import com.example.ikm.repositories.GenresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
/**
 * Сервисный класс для работы с книгами.
 * Содержит бизнес-логику операций с книгами.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Service - помечает класс как сервисный компонент Spring</li>
 * </ul>
 * </p>
 */
@Service
public class BooksService {
    private final BooksRepository bookRepository;
    private final AuthorsRepository authorRepository;
    private final GenresRepository genreRepository;
    /**
     * Конструктор с внедрением зависимостей репозиториев.
     *
     * @param bookRepository репозиторий для работы с книгами
     * @param authorRepository репозиторий для работы с авторами
     * @param genreRepository репозиторий для работы с жанрами
     */
    @Autowired
    public BooksService(BooksRepository bookRepository,
                        AuthorsRepository authorRepository,
                        GenresRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }
    /**
     * Получает список всех книг.
     *
     * @return список всех книг
     */
    public List<Books> getAllBooks() {
        return bookRepository.findAll();
    }
    /**
     * Находит книгу по идентификатору.
     *
     * @param id идентификатор книги
     * @return Optional с книгой, если найдена
     */
    public Optional<Books> getBookById(Long id) {
        return bookRepository.findById(id);
    }
    /**
     * Сохраняет новую книгу или обновляет существующую.
     * Выполняет валидацию и подготовку связанных объектов.
     *
     * @param book объект книги для сохранения
     * @return сохраненная книга
     */
    public Books saveBook(Books book) {
        validateAndPrepareBook(book);
        return bookRepository.save(book);
    }
    /**
     * Обновляет данные существующей книги.
     *
     * @param id идентификатор книги для обновления
     * @param bookDetails новые данные книги
     * @return обновленная книга
     * @throws RuntimeException если книга не найдена
     */
    public Books updateBook(Long id, Books bookDetails) {
        Books book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Книга не найдена"));

        book.setTitle(bookDetails.getTitle());
        book.setPublishYear(bookDetails.getPublishYear());
        book.setFeedback(bookDetails.getFeedback());

        // Обновляем автора если указан
        if (bookDetails.getAuthor() != null && bookDetails.getAuthor().getId() != null) {
            Authors author = authorRepository.findById(bookDetails.getAuthor().getId())
                    .orElseThrow(() -> new RuntimeException("Автор не найден"));
            book.setAuthor(author);
        }

        // Обновляем жанры если указаны
        if (bookDetails.getGenres() != null && !bookDetails.getGenres().isEmpty()) {
            Set<Genres> managedGenres = new HashSet<>();
            for (Genres genre : bookDetails.getGenres()) {
                if (genre.getId() != null) {
                    Genres managedGenre = genreRepository.findById(genre.getId())
                            .orElseThrow(() -> new RuntimeException("Жанр не найден"));
                    managedGenres.add(managedGenre);
                }
            }
            book.setGenres(managedGenres);
        }

        return bookRepository.save(book);
    }
    /**
     * Удаляет книгу по идентификатору.
     *
     * @param id идентификатор книги для удаления
     */
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
    /**
     * Выполняет поиск книг по различным критериям.
     * Поддерживает поиск по типу, автору и жанру.
     *
     * @param searchType тип поиска (title, author, year, feedback)
     * @param searchQuery поисковый запрос
     * @param authorId идентификатор автора для фильтрации
     * @param genreId идентификатор жанра для фильтрации
     * @return список книг, соответствующих критериям поиска
     */
    public List<Books> searchBooks(String searchType, String searchQuery, Long authorId, Long genreId) {
        List<Books> books;

        // Поиск по ID автора или жанра (приоритет)
        if (authorId != null) {
            books = bookRepository.findByAuthorId(authorId);
        } else if (genreId != null) {
            books = getBooksByGenreId(genreId);
        } else if (searchQuery == null || searchQuery.trim().isEmpty()) {
            books = bookRepository.findAll();
        } else {
            switch (searchType != null ? searchType : "title") {
                case "year":
                    try {
                        Integer year = Integer.parseInt(searchQuery);
                        books = bookRepository.findByPublishYear(year);
                    } catch (NumberFormatException e) {
                        books = List.of();
                    }
                    break;
                case "author":
                    books = searchByAuthorName(searchQuery);
                    break;
                case "feedback":
                    books = searchByFeedbackContaining(searchQuery);
                    break;
                default: // title
                    books = searchByTitleContaining(searchQuery);
                    break;
            }
        }

        return books;
    }
    /**
     * Получает книги по идентификатору автора.
     *
     * @param authorId идентификатор автора
     * @return список книг указанного автора
     */
    public List<Books> getBooksByAuthorId(Long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }
    /**
     * Получает книги по идентификатору жанра.
     *
     * @param genreId идентификатор жанра
     * @return список книг указанного жанра
     */
    public List<Books> getBooksByGenreId(Long genreId) {
        return filterBooksByGenre(genreId);
    }
    /**
     * Ищет книги по году публикации.
     *
     * @param year год публикации
     * @return список книг, опубликованных в указанный год
     */
    public List<Books> searchByPublishYear(Integer year) {
        return bookRepository.findByPublishYear(year);
    }
    /**
     * Ищет книги по имени автора.
     *
     * @param authorName имя или фамилия автора
     * @return список книг указанного автора
     */
    public List<Books> searchByAuthorName(String authorName) {
        return bookRepository.findByAuthorFirstNameContainingIgnoreCaseAndTitleContainingIgnoreCase(
                authorName, "");
    }
    /**
     * Ищет книги по содержанию отзыва.
     *
     * @param feedback текст для поиска в отзывах
     * @return список книг с отзывами, содержащими указанный текст
     */
    public List<Books> searchByFeedbackContaining(String feedback) {
        return bookRepository.findByFeedbackContainingIgnoreCase(feedback);
    }
    /**
     * Ищет книги по названию.
     *
     * @param title часть названия для поиска
     * @return список книг с названиями, содержащими указанный текст
     */
    public List<Books> searchByTitleContaining(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    /**
     * Валидирует и подготавливает книгу перед сохранением.
     * Проверяет существование автора и жанров, при необходимости загружает их из БД.
     *
     * @param book книга для валидации и подготовки
     */
    private void validateAndPrepareBook(Books book) {
        // Проверяем автора
        if (book.getAuthor() != null && book.getAuthor().getId() != null) {
            Authors author = authorRepository.findById(book.getAuthor().getId())
                    .orElseThrow(() -> new RuntimeException("Автор не найден"));
            book.setAuthor(author);
        }

        // Подготавливаем жанры
        if (book.getGenres() != null && !book.getGenres().isEmpty()) {
            Set<Genres> managedGenres = new HashSet<>();
            for (Genres genre : book.getGenres()) {
                if (genre.getId() != null) {
                    Genres managedGenre = genreRepository.findById(genre.getId())
                            .orElseThrow(() -> new RuntimeException("Жанр не найден"));
                    managedGenres.add(managedGenre);
                } else if (genre.getName() != null) {
                    Genres existingGenre = genreRepository.findByName(genre.getName())
                            .orElseGet(() -> {
                                Genres newGenre = new Genres(genre.getName());
                                return genreRepository.save(newGenre);
                            });
                    managedGenres.add(existingGenre);
                }
            }
            book.setGenres(managedGenres);
        }
    }
    /**
     * Фильтрует книги по идентификатору жанра.
     *
     * @param genreId идентификатор жанра
     * @return список книг, относящихся к указанному жанру
     */
    private List<Books> filterBooksByGenre(Long genreId) {
        List<Books> allBooks = bookRepository.findAll();
        return allBooks.stream()
                .filter(book -> book.getGenres().stream()
                        .anyMatch(genre -> genre.getId().equals(genreId)))
                .toList();
    }
    /**
     * Подсчитывает общее количество книг.
     *
     * @return количество книг
     */
    public long countBooks() {
        return bookRepository.count();
    }
    /**
     * Подсчитывает количество книг указанного автора.
     *
     * @param authorId идентификатор автора
     * @return количество книг автора
     */
    public long countBooksByAuthor(Long authorId) {
        return bookRepository.findByAuthorId(authorId).size();
    }
}