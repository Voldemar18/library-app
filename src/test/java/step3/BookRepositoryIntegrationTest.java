package step3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import ru.student.testing.LibraryApplication;
import ru.student.testing.model.Book;
import ru.student.testing.repository.BookRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@ContextConfiguration(classes = LibraryApplication.class)
@DataJpaTest
class BookRepositoryIntegrationTest {

    @Autowired
    BookRepository repository;

    @Test
    void shouldSaveAndFindByAuthor() {
        repository.save(new Book("Book1", "AuthorX", 2000));
        repository.save(new Book("Book2", "AuthorX", 2001));
        repository.save(new Book("Book3", "AuthorY", 2002));

        List<Book> result = repository.findByAuthor("AuthorX");

        assertThat(result).hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Book1", "Book2");
    }

    @Test
    void shouldAssignIdOnSave() {
        Book saved = repository.save(new Book("T", "A", 2020));
        assertThat(saved.getId()).isNotNull();
    }
}