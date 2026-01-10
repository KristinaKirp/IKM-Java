package com.example.ikm.repositories;

import com.example.ikm.entity.Authors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorsRepository extends JpaRepository<Authors, Long> {
    // Поиск авторов по имени
    List<Authors> findByFirstNameContainingIgnoreCase(String firstName);
    // Поиск авторов по фамилии
    List<Authors> findByLastNameContainingIgnoreCase(String lastName);
    // Поиск авторов по году рождения
    List<Authors> findByBirthYear(Integer birthYear);
    // Поиск авторов по диапазону годов рождения
    List<Authors> findByBirthYearBetween(Integer startYear, Integer endYear);
    // Поиск по имени и фамилии
    List<Authors> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
    Optional<Authors> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}