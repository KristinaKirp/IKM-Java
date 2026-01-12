package com.example.ikm.controller;

import com.example.ikm.entity.Books;
import com.example.ikm.service.BooksService;
import com.example.ikm.service.AuthorsService;
import com.example.ikm.service.GenresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
/**
 * Контроллер для управления книгами в системе библиотеки.
 * Обрабатывает HTTP-запросы, связанные с операциями CRUD для книг.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Controller - указывает, что класс является контроллером Spring MVC</li>
 *   <li>@RequestMapping("/books") - определяет базовый URL для всех методов контроллера</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/books")
public class BooksController {
    private final BooksService bookService;
    private final AuthorsService authorService;
    private final GenresService genreService;
    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param bookService сервис для работы с книгами
     * @param authorService сервис для работы с авторами
     * @param genreService сервис для работы с жанрами
     */
    @Autowired
    public BooksController(BooksService bookService,
                           AuthorsService authorService,
                           GenresService genreService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.genreService = genreService;
    }
    /**
     * Отображает список всех книг.
     *
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона для отображения списка книг
     */
    @GetMapping
    public String listBooks(Model model) {
        List<Books> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("bookCount", books.size());
        prepareSearchModel(model);
        return "books/list";
    }
    /**
     * Выполняет поиск книг по различным критериям.
     *
     * @param searchType тип поиска (title, author, year, feedback)
     * @param searchQuery поисковый запрос
     * @param authorId идентификатор автора для фильтрации
     * @param genreId идентификатор жанра для фильтрации
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона для отображения результатов поиска
     */
    @GetMapping("/search")
    public String searchBooks(@RequestParam(required = false) String searchType,
                              @RequestParam(required = false) String searchQuery,
                              @RequestParam(required = false) Long authorId,
                              @RequestParam(required = false) Long genreId,
                              Model model) {
        List<Books> books = bookService.searchBooks(searchType, searchQuery, authorId, genreId);

        model.addAttribute("books", books);
        model.addAttribute("bookCount", books.size());
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("selectedAuthorId", authorId);
        model.addAttribute("selectedGenreId", genreId);
        prepareSearchModel(model);

        return "books/list";
    }
    /**
     * Отображает форму для создания новой книги.
     *
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона формы создания книги
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Books());
        model.addAttribute("action", "create");
        // Не нужно authors/genres — они вводятся текстом
        return "books/form";
    }
    /**
     * Обрабатывает создание новой книги.
     * Автор и жанры создаются автоматически, если не существуют.
     *
     * @param title название книги
     * @param authorFirstName имя автора
     * @param authorLastName фамилия автора
     * @param publishYear год публикации
     * @param genreInput строка с жанрами через запятую
     * @param feedback отзыв о книге (опционально)
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @param model объект Model для передачи данных в представление
     * @return перенаправление на список книг или возврат к форме при ошибках
     */
    @PostMapping
    public String createBook(
            @RequestParam String title,
            @RequestParam String authorFirstName,
            @RequestParam String authorLastName,
            @RequestParam Integer publishYear,
            @RequestParam String genreInput,
            @RequestParam(required = false) String feedback,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            var author = authorService.findOrCreateAuthor(authorFirstName, authorLastName);
            var genres = genreService.findOrCreateGenresFromInput(genreInput);

            Books book = new Books();
            book.setTitle(title);
            book.setAuthor(author);
            book.setPublishYear(publishYear);
            book.setFeedback(feedback);
            book.setGenres(genres);

            bookService.saveBook(book);

            redirectAttributes.addFlashAttribute("successMessage", "Книга успешно добавлена");
            return "redirect:/books";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка: " + e.getMessage());
            model.addAttribute("book", new Books());
            model.addAttribute("action", "create");
            return "books/form";
        }
    }
    /**
     * Отображает форму для редактирования существующей книги.
     *
     * @param id идентификатор книги для редактирования
     * @param model объект Model для передачи данных в представление
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return имя шаблона формы редактирования или перенаправление при ошибке
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Books book = bookService.getBookById(id).orElse(null);

        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Книга не найдена");
            return "redirect:/books";
        }

        model.addAttribute("book", book);
        model.addAttribute("action", "edit");
        return "books/form";
    }
    /**
     * Обрабатывает обновление данных книги.
     *
     * @param id идентификатор книги для обновления
     * @param title новое название книги
     * @param authorFirstName новое имя автора
     * @param authorLastName новая фамилия автора
     * @param publishYear новый год публикации
     * @param genreInput новая строка с жанрами через запятую
     * @param feedback новый отзыв о книге (опционально)
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @param model объект Model для передачи данных в представление
     * @return перенаправление на список книг или возврат к форме при ошибках
     */
    @PostMapping("/update/{id}")
    public String updateBook(
            @PathVariable("id") Long id,
            @RequestParam String title,
            @RequestParam String authorFirstName,
            @RequestParam String authorLastName,
            @RequestParam Integer publishYear,
            @RequestParam String genreInput,
            @RequestParam(required = false) String feedback,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            Books existingBook = bookService.getBookById(id)
                    .orElseThrow(() -> new RuntimeException("Книга не найдена"));

            var author = authorService.findOrCreateAuthor(authorFirstName, authorLastName);
            var genres = genreService.findOrCreateGenresFromInput(genreInput);

            existingBook.setTitle(title);
            existingBook.setAuthor(author);
            existingBook.setPublishYear(publishYear);
            existingBook.setFeedback(feedback);
            existingBook.setGenres(genres);

            bookService.saveBook(existingBook);

            redirectAttributes.addFlashAttribute("successMessage", "Книга успешно обновлена");
            return "redirect:/books";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка: " + e.getMessage());
            model.addAttribute("book", bookService.getBookById(id).orElse(new Books()));
            model.addAttribute("action", "edit");
            return "books/form";
        }
    }
    /**
     * Удаляет книгу по идентификатору.
     *
     * @param id идентификатор книги для удаления
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return перенаправление на список книг
     */
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Books book = bookService.getBookById(id).orElse(null);
            if (book != null) {
                bookService.deleteBook(id);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Книга \"" + book.getTitle() + "\" успешно удалена");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Книга не найдена");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении книги: " + e.getMessage());
        }
        return "redirect:/books";
    }
    /**
     * Отображает подробную информацию о книге.
     *
     * @param id идентификатор книги
     * @param model объект Model для передачи данных в представление
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return имя шаблона для отображения информации о книге
     */
    @GetMapping("/view/{id}")
    public String viewBook(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Books book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Книга не найдена");
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        return "books/view";
    }
    /**
     * Подготавливает модель для поиска, добавляя списки авторов и жанров.
     *
     * @param model объект Model для передачи данных в представление
     */
    private void prepareSearchModel(Model model) {
        model.addAttribute("authors", authorService.getAllAuthors());
        model.addAttribute("genres", genreService.getAllGenres());
    }
}