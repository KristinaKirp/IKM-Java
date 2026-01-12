package com.example.ikm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * Основной контроллер для домашней страницы.
 * Перенаправляет запросы на главную страницу на список книг.
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Controller - указывает, что класс является контроллером Spring MVC</li>
 * </ul>
 * </p>
 */
@Controller
public class HomeController {

    /**
     * Обрабатывает запрос к корневому URL и перенаправляет на страницу книг.
     *
     * @return перенаправление на список книг
     */
    @GetMapping("/")
    public String homepage() {
        return "redirect:/books";
    }
}
