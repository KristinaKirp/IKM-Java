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

@Service
public class BooksService {
    private final BooksRepository bookRepository;
    private final AuthorsRepository authorRepository;
    private final GenresRepository genreRepository;

    @Autowired
    public BooksService(BooksRepository bookRepository,
                        AuthorsRepository authorRepository,
                        GenresRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }

    public List<Books> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Books> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Books saveBook(Books book) {
        validateAndPrepareBook(book);
        return bookRepository.save(book);
    }

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

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

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


    public List<Books> getBooksByAuthorId(Long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }

    public List<Books> getBooksByGenreId(Long genreId) {
        return filterBooksByGenre(genreId);
    }

    public List<Books> searchByPublishYear(Integer year) {
        return bookRepository.findByPublishYear(year);
    }

    public List<Books> searchByAuthorName(String authorName) {
        return bookRepository.findByAuthorFirstNameContainingIgnoreCaseAndTitleContainingIgnoreCase(
                authorName, "");
    }

    public List<Books> searchByFeedbackContaining(String feedback) {
        return bookRepository.findByFeedbackContainingIgnoreCase(feedback);
    }

    public List<Books> searchByTitleContaining(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Books> searchBooksByYearRange(Integer startYear, Integer endYear) {
        if (startYear != null && endYear != null) {
            return bookRepository.findByPublishYearBetween(startYear, endYear);
        } else if (startYear != null) {
            return bookRepository.findByPublishYear(startYear);
        } else {
            return bookRepository.findAll();
        }
    }


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

    private List<Books> filterBooksByGenre(Long genreId) {
        List<Books> allBooks = bookRepository.findAll();
        return allBooks.stream()
                .filter(book -> book.getGenres().stream()
                        .anyMatch(genre -> genre.getId().equals(genreId)))
                .toList();
    }

    public long countBooks() {
        return bookRepository.count();
    }

    public long countBooksByAuthor(Long authorId) {
        return bookRepository.findByAuthorId(authorId).size();
    }
}