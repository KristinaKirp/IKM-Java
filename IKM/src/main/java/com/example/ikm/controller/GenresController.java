package com.example.ikm.controller;

import com.example.ikm.entity.Genres;
import com.example.ikm.service.GenresService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
/**
 * Контроллер для управления жанрами в системе библиотеки.
 * Обрабатывает HTTP-запросы, связанные с операциями CRUD для жанров.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Controller - указывает, что класс является контроллером Spring MVC</li>
 *   <li>@RequestMapping("/genres") - определяет базовый URL для всех методов контроллера</li>
 * </ul>
 * </p>
 */
@Controller
@RequestMapping("/genres")
public class GenresController {
    private final GenresService genreService;
    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param genreService сервис для работы с жанрами
     */
    @Autowired
    public GenresController(GenresService genreService) {
        this.genreService = genreService;
    }
    /**
     * Отображает список всех жанров.
     *
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона для отображения списка жанров
     */
    @GetMapping
    public String listGenres(Model model) {
        List<Genres> genres = genreService.getAllGenres();
        model.addAttribute("genres", genres);
        model.addAttribute("genreCount", genres.size());
        return "genres/list";
    }

    /**
     * Выполняет поиск жанров по названию.
     *
     * @param searchQuery поисковый запрос
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона для отображения результатов поиска
     */
    @GetMapping("/search")
    public String searchGenres(@RequestParam(required = false) String searchQuery,
                               Model model) {
        List<Genres> genres = genreService.searchGenres(searchQuery);

        model.addAttribute("genres", genres);
        model.addAttribute("genreCount", genres.size());
        model.addAttribute("searchQuery", searchQuery);

        return "genres/list";
    }
    /**
     * Отображает форму для создания нового жанра.
     *
     * @param model объект Model для передачи данных в представление
     * @return имя шаблона формы создания жанра
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("genre", new Genres());
        return "genres/form";
    }
    /**
     * Обрабатывает создание нового жанра.
     *
     * @param genre объект жанра с данными из формы
     * @param result объект для проверки валидации
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return перенаправление на список жанров или возврат к форме при ошибках
     */
    @PostMapping
    public String createGenre(@Valid @ModelAttribute("genre") Genres genre,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "genres/form";
        }

        try {
            genreService.saveGenre(genre);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Жанр \"" + genre.getName() + "\" успешно добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: " + e.getMessage());
        }

        return "redirect:/genres";
    }
    /**
     * Удаляет жанр по идентификатору.
     * Проверяет, используется ли жанр в книгах перед удалением.
     *
     * @param id идентификатор жанра для удаления
     * @param redirectAttributes атрибуты для перенаправления с сообщениями
     * @return перенаправление на список жанров
     */
    @GetMapping("/delete/{id}")
    public String deleteGenre(@PathVariable("id") Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            Genres genre = genreService.getGenreById(id).orElse(null);
            if (genre != null) {
                // Проверяем, используется ли жанр в книгах
                if (genreService.isGenreUsed(id)) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Жанр используется в книгах и не может быть удален");
                    return "redirect:/genres";
                }

                genreService.deleteGenre(id);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Жанр " + genre.getName() + " успешно удален");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Жанр не найден");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении жанра: " + e.getMessage());
        }

        return "redirect:/genres";
    }
}