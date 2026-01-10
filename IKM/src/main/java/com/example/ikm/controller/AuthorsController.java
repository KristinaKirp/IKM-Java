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

@Controller
@RequestMapping("/authors")
public class AuthorsController {
    private final AuthorsService authorService;
    private final BooksService bookService;

    @Autowired
    public AuthorsController(AuthorsService authorService, BooksService bookService) {
        this.authorService = authorService;
        this.bookService = bookService;
    }

    @GetMapping
    public String listAuthors(Model model) {
        List<Authors> authors = authorService.getAllAuthors();
        model.addAttribute("authors", authors);
        model.addAttribute("authorCount", authors.size());
        return "authors/list";
    }

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

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("author", new Authors());
        return "authors/form";
    }

    @PostMapping
    public String createAuthor(@Valid @ModelAttribute("author") Authors author,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "authors/form";
        }

        authorService.saveAuthor(author);
        redirectAttributes.addFlashAttribute("success", "Автор успешно добавлен");
        return "redirect:/authors";
    }

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

    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        authorService.deleteAuthor(id);
        redirectAttributes.addFlashAttribute("success", "Автор успешно удален");
        return "redirect:/authors";
    }
}