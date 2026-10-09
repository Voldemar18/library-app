package kt6_step_reverse.step1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.student.testing.LibraryApplication;
import ru.student.testing.model.Book;
import ru.student.testing.repository.BookRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@ContextConfiguration(classes = LibraryApplication.class)
@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryBottomUpTest {

    @Autowired
    BookRepository repository;

    @Test
    void shouldAssignIdOnSave() {
        Book saved = repository.save(new Book("Clean Code", "R. Martin", 2008));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindById() {
        Book saved = repository.save(new Book("Effective Java", "J. Bloch", 2018));

        Optional<Book> found = repository.findById(saved.getId());

        assertThat(found).isPresent()
                .get()
                .extracting(Book::getTitle)
                .isEqualTo("Effective Java");
    }

    @Test
    void shouldFindByAuthor() {
        repository.save(new Book("B1", "AuthorX", 2000));
        repository.save(new Book("B2", "AuthorX", 2001));
        repository.save(new Book("B3", "AuthorY", 2002));

        List<Book> result = repository.findByAuthor("AuthorX");

        assertThat(result).hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("B1", "B2");
    }

    @Test
    void shouldDeleteById() {
        Book saved = repository.save(new Book("T", "A", 2000));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoBooks() {
        assertThat(repository.findAll()).isEmpty();
    }
}