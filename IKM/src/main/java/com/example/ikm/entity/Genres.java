package com.example.ikm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "genres")
public class Genres {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название жанра обязательно")
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "genres")
    private Set<Books> books = new HashSet<>();

    // Конструкторы
    public Genres() {}

    public Genres(String name) {
        this.name = name;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Books> getBooks() { return books; }
    public void setBooks(Set<Books> books) { this.books = books; }
}
