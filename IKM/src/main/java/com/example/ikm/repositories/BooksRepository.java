package com.example.ikm.repositories;

import com.example.ikm.entity.Books;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BooksRepository extends JpaRepository<Books, Long> {

    // Поиск книг по названию
    List<Books> findByTitleContainingIgnoreCase(String title);

    // Поиск книг по году публикации
    List<Books> findByPublishYear(Integer publishYear);

    // Поиск книг по диапазону годов публикации
    List<Books> findByPublishYearBetween(Integer startYear, Integer endYear);

    // Поиск книг по автору (ID)
    List<Books> findByAuthorId(Long authorId);

    // Поиск книг по отзыву
    List<Books> findByFeedbackContainingIgnoreCase(String feedback);

    // Поиск книг по автору и названию
    List<Books> findByAuthorFirstNameContainingIgnoreCaseAndTitleContainingIgnoreCase(
            String authorFirstName, String title);
}