package com.example.ikm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

@Entity
@Table(name = "authors")
public class Authors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Column(name = "last_name")
    private String lastName;

    @Min(value = 1000, message = "Некорректный год рождения")
    @Column(name = "birth_year")
    private Integer birthYear;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Books> books;

    // Геттеры, сеттеры и вспомогательные методы
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Конструкторы
    public Authors() {}

    public Authors(String firstName, String lastName, Integer birthYear) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public List<Books> getBooks() { return books; }
    public void setBooks(List<Books> books) { this.books = books; }
}