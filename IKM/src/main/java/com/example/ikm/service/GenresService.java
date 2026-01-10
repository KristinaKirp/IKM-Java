package com.example.ikm.service;

import com.example.ikm.entity.Books;
import com.example.ikm.entity.Genres;
import com.example.ikm.repositories.GenresRepository;
import com.example.ikm.repositories.BooksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GenresService {
    private final GenresRepository genreRepository;
    private final BooksRepository booksRepository;

    @Autowired
    public GenresService(GenresRepository genreRepository, BooksRepository booksRepository) {
        this.genreRepository = genreRepository;
        this.booksRepository = booksRepository;
    }

    public List<Genres> getAllGenres() {
        return genreRepository.findAll();
    }

    public Optional<Genres> getGenreById(Long id) {
        return genreRepository.findById(id);
    }

    public Genres saveGenre(Genres genre) {
        if (genreRepository.existsByName(genre.getName())) {
            throw new RuntimeException("Жанр '" + genre.getName() + "' уже существует");
        }
        return genreRepository.save(genre);
    }

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

    public void deleteGenre(Long id) {
        if (isGenreUsed(id)) {
            throw new RuntimeException("Нельзя удалить жанр, который используется в книгах");
        }
        genreRepository.deleteById(id);
    }

    public List<Genres> searchGenres(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return genreRepository.findAll();
        } else {
            return genreRepository.findByNameContainingIgnoreCase(searchQuery);
        }
    }

    public Optional<Genres> findGenreByName(String name) {
        return genreRepository.findByName(name);
    }

    public boolean genreExists(String name) {
        return genreRepository.existsByName(name);
    }

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

    public Genres getOrCreateGenre(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название жанра не может быть пустым");
        }
        String cleanName = name.trim();
        return genreRepository.findByName(cleanName)
                .orElseGet(() -> {
                    Genres newGenre = new Genres();
                    newGenre.setName(cleanName);
                    return genreRepository.save(newGenre);
                });
    }

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

    public long countGenres() {
        return genreRepository.count();
    }
}