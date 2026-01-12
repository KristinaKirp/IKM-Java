package com.example.ikm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.HashSet;
import java.util.Set;
/**
 * Сущность, представляющая книгу в системе библиотеки.
 * Связана с автором отношением "многие к одному" и с жанрами отношением "многие ко многим".
 *
 * <p>Аннотации:
 * <ul>
 *   <li>@Entity - указывает, что класс является JPA сущностью</li>
 *   <li>@Table(name = "books") - задает имя таблицы в БД</li>
 *   <li>@ManyToOne - отношение "много книг - один автор"</li>
 *   <li>@ManyToMany - отношение "много книг - много жанров"</li>
 *   <li>@JoinTable - определяет таблицу связи для ManyToMany</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "books")
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название обязательно")
    private String title;

    @NotNull(message = "Год публикации обязателен")
    @Min(value = 1000, message = "Некорректный год публикации")
    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @NotNull(message = "Автор обязателен")
    private Authors author;

    @ManyToMany
    @JoinTable(
            name = "book_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genres> genres = new HashSet<>();

    // Конструкторы
    public Books() {}

    public Books(String title, Integer publishYear, Authors author) {
        this.title = title;
        this.publishYear = publishYear;
        this.author = author;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Authors getAuthor() { return author; }
    public void setAuthor(Authors author) { this.author = author; }

    public Set<Genres> getGenres() { return genres; }
    public void setGenres(Set<Genres> genres) { this.genres = genres; }
}