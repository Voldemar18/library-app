package ru.student.testing.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.student.testing.model.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthor(String author);
}