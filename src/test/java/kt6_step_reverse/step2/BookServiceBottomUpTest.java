package kt6_step_reverse.step2;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.student.testing.LibraryApplication;
import ru.student.testing.model.Book;
import ru.student.testing.repository.BookRepository;
import ru.student.testing.service.BookService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
@ContextConfiguration(classes = LibraryApplication.class)
@DataJpaTest
@Import(BookService.class)
@ActiveProfiles("test")
class BookServiceBottomUpTest {

    @Autowired BookService service;
    @Autowired BookRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void add_shouldPersistBook() {
        Book saved = service.add(new Book("Refactoring", "M. Fowler", 1999));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void add_shouldRejectInvalidYear() {
        Book invalid = new Book("Bad", "A", 3000);

        assertThatThrownBy(() -> service.add(invalid))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(repository.count()).isZero();
    }

    @Test
    void findById_shouldReturnEmptyWhenMissing() {
        assertThat(service.findById(999L)).isEmpty();
    }

    @Test
    void findByAuthor_shouldReturnFilteredList() {
        service.add(new Book("B1", "AuthorA", 2000));
        service.add(new Book("B2", "AuthorA", 2001));
        service.add(new Book("B3", "AuthorB", 2002));

        List<Book> result = service.findByAuthor("AuthorA");

        assertThat(result).hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("B1", "B2");
    }

    @Test
    void delete_shouldRemoveFromRepository() {
        Book saved = service.add(new Book("T", "A", 2000));

        service.delete(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
