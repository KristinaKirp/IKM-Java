package com.example.ikm.service;

import com.example.ikm.entity.Books;
import com.example.ikm.entity.Genres;
import com.example.ikm.repositories.GenresRepository;
import com.example.ikm.repositories.BooksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
/**
 * Сервисный класс для работы с жанрами.
 * Содержит бизнес-логику операций с жанрами.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Service - помечает класс как сервисный компонент Spring</li>
 * </ul>
 * </p>
 */
@Service
public class GenresService {
    private final GenresRepository genreRepository;
    private final BooksRepository booksRepository;
    /**
     * Конструктор с внедрением зависимостей репозиториев.
     *
     * @param genreRepository репозиторий для работы с жанрами
     * @param booksRepository репозиторий для работы с книгами
     */
    @Autowired
    public GenresService(GenresRepository genreRepository, BooksRepository booksRepository) {
        this.genreRepository = genreRepository;
        this.booksRepository = booksRepository;
    }
    /**
     * Получает список всех жанров.
     *
     * @return список всех жанров
     */
    public List<Genres> getAllGenres() {
        return genreRepository.findAll();
    }
    /**
     * Находит жанр по идентификатору.
     *
     * @param id идентификатор жанра
     * @return Optional с жанром, если найден
     */
    public Optional<Genres> getGenreById(Long id) {
        return genreRepository.findById(id);
    }
    /**
     * Сохраняет новый жанр.
     * Проверяет уникальность названия жанра.
     *
     * @param genre объект жанра для сохранения
     * @return сохраненный жанр
     * @throws RuntimeException если жанр с таким названием уже существует
     */
    public Genres saveGenre(Genres genre) {
        String normalizedName = capitalizeFirst(genre.getName().trim().toLowerCase());
        if (genreRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new RuntimeException("Жанр '" + normalizedName + "' уже существует");
        }
        genre.setName(normalizedName); // сохраняем в нормализованном виде
        return genreRepository.save(genre);
    }
    /**
     * Обновляет данные существующего жанра.
     *
     * @param id идентификатор жанра для обновления
     * @param genreDetails новые данные жанра
     * @return обновленный жанр
     * @throws RuntimeException если жанр не найден или новое название уже существует
     */
    public Genres updateGenre(Long id, Genres genreDetails) {
        Genres genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Жанр не найден"));

        if (!genre.getName().equals(genreDetails.getName()) &&
                genreRepository.existsByName(genreDetails.getName())) {
            throw new RuntimeException("Жанр '" + genreDetails.getName() + "' уже существует");
        }

        genre.setName(genreDetails.getName());
        return genreRepository.save(genre);
    }
    /**
     * Удаляет жанр по идентификатору.
     * Проверяет, используется ли жанр в книгах.
     *
     * @param id идентификатор жанра для удаления
     * @throws RuntimeException если жанр используется в книгах
     */
    public void deleteGenre(Long id) {
        if (isGenreUsed(id)) {
            throw new RuntimeException("Нельзя удалить жанр, который используется в книгах");
        }
        genreRepository.deleteById(id);
    }
    /**
     * Ищет жанры по названию.
     *
     * @param searchQuery текст для поиска в названиях жанров
     * @return список жанров, содержащих указанный текст в названии
     */
    public List<Genres> searchGenres(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return genreRepository.findAll();
        } else {
            return genreRepository.findByNameContainingIgnoreCase(searchQuery);
        }
    }
    /**
     * Находит жанр по точному названию.
     *
     * @param name точное название жанра
     * @return Optional с жанром, если найден
     */
    public Optional<Genres> findGenreByName(String name) {
        return genreRepository.findByName(name);
    }
    /**
     * Проверяет существование жанра по названию.
     *
     * @param name название жанра для проверки
     * @return true, если жанр существует, иначе false
     */
    public boolean genreExists(String name) {
        return genreRepository.existsByName(name);
    }
    /**
     * Проверяет, используется ли жанр в каких-либо книгах.
     *
     * @param genreId идентификатор жанра
     * @return true, если жанр используется в книгах, иначе false
     */
    public boolean isGenreUsed(Long genreId) {
        List<Books> allBooks = booksRepository.findAll();
        for (Books book : allBooks) {
            if (book.getGenres().stream()
                    .anyMatch(genre -> genre.getId().equals(genreId))) {
                return true;
            }
        }
        return false;
    }
    /**
     * Находит существующий жанр или создает новый, если не найден.
     *
     * @param name название жанра
     * @return существующий или созданный жанр
     * @throws IllegalArgumentException если название пустое
     */
    public Genres getOrCreateGenre(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название жанра не может быть пустым");
        }
        String cleanName = name.trim();

        // Ищем без учёта регистра
        return genreRepository.findByNameIgnoreCase(cleanName)
                .orElseGet(() -> {
                    // Перед сохранением нормализуем регистр (например, с заглавной буквы)
                    String normalized = capitalizeFirst(cleanName.toLowerCase());
                    Genres newGenre = new Genres();
                    newGenre.setName(normalized);
                    return genreRepository.save(newGenre);
                });
    }

    private String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Создает или находит жанры из строки ввода, разделенной запятыми.
     *
     * @param input строка с жанрами, разделенными запятыми
     * @return множество жанров
     * @throws IllegalArgumentException если строка пустая или не удалось извлечь жанры
     */
    public Set<Genres> findOrCreateGenresFromInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Жанры не указаны");
        }

        Set<Genres> genres = new HashSet<>();
        String[] parts = input.split(",");

        for (String part : parts) {
            String name = part.trim();
            if (!name.isEmpty()) {
                Genres genre = getOrCreateGenre(name);
                genres.add(genre);
            }
        }

        if (genres.isEmpty()) {
            throw new IllegalArgumentException("Не удалось извлечь жанры из ввода");
        }

        return genres;
    }
    /**
     * Подсчитывает общее количество жанров.
     *
     * @return количество жанров
     */
    public long countGenres() {
        return genreRepository.count();
    }
}