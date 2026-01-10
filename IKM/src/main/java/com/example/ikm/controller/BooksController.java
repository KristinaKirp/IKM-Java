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

@Controller
@RequestMapping("/books")
public class BooksController {
    private final BooksService bookService;
    private final AuthorsService authorService;
    private final GenresService genreService;

    @Autowired
    public BooksController(BooksService bookService,
                           AuthorsService authorService,
                           GenresService genreService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.genreService = genreService;
    }

    @GetMapping
    public String listBooks(Model model) {
        List<Books> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("bookCount", books.size());
        prepareSearchModel(model);
        return "books/list";
    }

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

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Books());
        model.addAttribute("action", "create");
        // Не нужно authors/genres — они вводятся текстом
        return "books/form";
    }

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

    private void prepareSearchModel(Model model) {
        model.addAttribute("authors", authorService.getAllAuthors());
        model.addAttribute("genres", genreService.getAllGenres());
    }
}