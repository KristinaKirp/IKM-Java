package com.example.ikm.controller;

import com.example.ikm.entity.Authors;
import com.example.ikm.service.AuthorsService;
import com.example.ikm.service.BooksService;
import com.example.ikm.entity.Books;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Контроллер для управления авторами в системе библиотеки.
 * Обрабатывает HTTP-запросы, связанные с операциями CRUD для авторов.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Controller - указывает, что класс является контроллером Spring MVC</li>
 *   <li>@RequestMapping("/authors") - определяет базовый URL для всех методов контроллера</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/authors")
public class AuthorsController {
    private final AuthorsService authorService;
    private final BooksService bookService;
    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param authorService сервис для работы с авторами
     * @param bookService сервис для работы с книгами
     */
    @Autowired
    public AuthorsController(AuthorsService authorService, BooksService bookService) {
        this.authorService = authorService;
        this.bookService = bookService;
    }
    /**
     * Отображает список всех авторов.
     *
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона для отображения списка авторов
     */
    @GetMapping
    public String listAuthors(Model model) {
        List<Authors> authors = authorService.getAllAuthors();
        model.addAttribute("authors", authors);
        model.addAttribute("authorCount", authors.size());
        return "authors/list";
    }
    /**
     * Выполняет поиск авторов по различным критериям.
     *
     * @param searchType тип поиска (firstName, lastName, birthYear, fullName)
     * @param searchQuery поисковый запрос
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона для отображения результатов поиска
     */
    @GetMapping("/search")
    public String searchAuthors(@RequestParam(required = false) String searchType,
                                @RequestParam(required = false) String searchQuery,
                                Model model) {
        List<Authors> authors = authorService.searchAuthors(searchType, searchQuery);

        model.addAttribute("authors", authors);
        model.addAttribute("authorCount", authors.size());
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchQuery", searchQuery);

        return "authors/list";
    }
    /**
     * Отображает подробную информацию об авторе, включая его книги.
     *
     * @param id идентификатор автора
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона для отображения информации об авторе
     * @throws RuntimeException если автор не найден
     */
    @GetMapping("/view/{id}")
    public String viewAuthor(@PathVariable Long id, Model model) {
        Authors author = authorService.getAuthorById(id)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));

        List<Books> books = bookService.getBooksByAuthorId(id);

        model.addAttribute("author", author);
        model.addAttribute("books", books);
        model.addAttribute("bookCount", books.size());

        return "authors/view";
    }
    /**
     * Отображает форму для создания нового автора.
     *
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона формы создания автора
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("author", new Authors());
        return "authors/form";
    }
    /**
     * Обрабатывает создание нового автора.
     *
     * @param author объект автора с данными из формы
     * @param result объект для проверки валидации
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return перенаправление на список авторов или возврат к форме при ошибках
     */
    @PostMapping
    public String createAuthor(@Valid @ModelAttribute("author") Authors author,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        // Сначала проверяем валидацию
        if (result.hasErrors()) {
            return "authors/form";
        }

        // Проверяем, существует ли автор с таким именем и фамилией (игнорируя регистр)
        boolean exists = authorService.authorExists(author.getFirstName(), author.getLastName());
        if (exists) {
            model.addAttribute("author", author); // чтобы форма сохранила введённые данные
            model.addAttribute("errorMessage",
                    "Автор \"" + author.getFirstName() + " " + author.getLastName() + "\" уже существует.");
            return "authors/form"; // остаёмся на форме с ошибкой
        }

        // Если всё ок — сохраняем
        authorService.saveAuthor(author);
        redirectAttributes.addFlashAttribute("success", "Автор успешно добавлен");
        return "redirect:/authors";
    }
    /**
     * Отображает форму для редактирования существующего автора.
     *
     * @param id идентификатор автора для редактирования
     * @param model объект Model для передачи данных в представление
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return имя шаблона формы редактирования или перенаправление при ошибке
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        Authors author = authorService.getAuthorById(id)
                .orElse(null);

        if (author == null) {
            redirectAttributes.addFlashAttribute("error", "Автор не найден");
            return "redirect:/authors";
        }

        model.addAttribute("author", author);
        return "authors/form";
    }
    /**
     * Обрабатывает обновление данных автора.
     *
     * @param id идентификатор автора для обновления
     * @param author обновленные данные автора
     * @param result объект для проверки валидации
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return перенаправление на список авторов или возврат к форме при ошибках
     */
    @PostMapping("/update/{id}")
    public String updateAuthor(@PathVariable Long id,
                               @Valid @ModelAttribute("author") Authors author,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "authors/form";
        }

        authorService.updateAuthor(id, author);
        redirectAttributes.addFlashAttribute("success", "Автор успешно обновлен");
        return "redirect:/authors";
    }
    /**
     * Удаляет автора по идентификатору.
     *
     * @param id идентификатор автора для удаления
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return перенаправление на список авторов
     */
    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        authorService.deleteAuthor(id);
        redirectAttributes.addFlashAttribute("success", "Автор успешно удален");
        return "redirect:/authors";
    }
}