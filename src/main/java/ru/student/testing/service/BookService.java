package ru.student.testing.service;


import org.springframework.stereotype.Service;
import ru.student.testing.model.Book;
import ru.student.testing.repository.BookRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll() {
        return repository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return repository.findById(id);
    }

    public Book add(Book book) {
        if (book.getPublishYear() < 0 || book.getPublishYear() > 2100) {
            throw new IllegalArgumentException("Invalid year");
        }
        return repository.save(book);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Book> findByAuthor(String author) {
        return repository.findByAuthor(author);
    }
}
