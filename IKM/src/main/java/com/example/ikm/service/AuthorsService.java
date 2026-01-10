package com.example.ikm.service;

import com.example.ikm.entity.Authors;
import com.example.ikm.repositories.AuthorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorsService {
    private final AuthorsRepository authorRepository;

    @Autowired
    public AuthorsService(AuthorsRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<Authors> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Optional<Authors> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }

    public Authors saveAuthor(Authors author) {
        return authorRepository.save(author);
    }

    public Authors updateAuthor(Long id, Authors authorDetails) {
        Authors author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автор не найден"));

        author.setFirstName(authorDetails.getFirstName());
        author.setLastName(authorDetails.getLastName());
        author.setBirthYear(authorDetails.getBirthYear());

        return authorRepository.save(author);
    }

    public void deleteAuthor(Long id) {
        authorRepository.deleteById(id);
    }

    public List<Authors> searchAuthors(String searchType, String searchQuery) {
        List<Authors> authors;

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            authors = authorRepository.findAll();
        } else {
            switch (searchType != null ? searchType : "lastName") {
                case "firstName":
                    authors = authorRepository.findByFirstNameContainingIgnoreCase(searchQuery);
                    break;
                case "birthYear":
                    try {
                        Integer year = Integer.parseInt(searchQuery);
                        authors = authorRepository.findByBirthYear(year);
                    } catch (NumberFormatException e) {
                        authors = List.of();
                    }
                    break;
                case "fullName":
                    authors = authorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                            searchQuery, searchQuery);
                    break;
                default:
                    authors = authorRepository.findByLastNameContainingIgnoreCase(searchQuery);
                    break;
            }
        }

        return authors;
    }

    public List<Authors> searchAuthorsByYearRange(Integer startYear, Integer endYear) {
        if (startYear != null && endYear != null) {
            return authorRepository.findByBirthYearBetween(startYear, endYear);
        } else if (startYear != null) {
            return authorRepository.findByBirthYear(startYear);
        } else {
            return authorRepository.findAll();
        }
    }

    public long countAuthors() {
        return authorRepository.count();
    }

    public Authors findOrCreateAuthor(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя и фамилия автора обязательны");
        }

        String cleanFirstName = firstName.trim();
        String cleanLastName = lastName.trim();

        return authorRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(cleanFirstName, cleanLastName)
                .orElseGet(() -> {
                    Authors newAuthor = new Authors();
                    newAuthor.setFirstName(cleanFirstName);
                    newAuthor.setLastName(cleanLastName);
                    return authorRepository.save(newAuthor);
                });
    }
}