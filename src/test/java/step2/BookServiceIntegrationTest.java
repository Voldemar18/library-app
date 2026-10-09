package step2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import ru.student.testing.LibraryApplication;
import ru.student.testing.model.Book;
import ru.student.testing.repository.BookRepository;
import ru.student.testing.service.BookService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ContextConfiguration(classes = LibraryApplication.class)
@ExtendWith(MockitoExtension.class)
class BookServiceIntegrationTest {

    @Mock
    BookRepository repository;
    @InjectMocks
    BookService service;

    @Test
    void add_shouldPersistAndReturnBook() {
        Book b = new Book("Effective Java", "J. Bloch", 2018);
        when(repository.save(b)).thenReturn(b);

        Book saved = service.add(b);

        assertThat(saved.getTitle()).isEqualTo("Effective Java");
        verify(repository).save(b);
    }

    @Test
    void add_shouldRejectInvalidYear() {
        Book b = new Book("Bad", "A", 3000);

        assertThatThrownBy(() -> service.add(b))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void findById_shouldReturnEmptyWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThat(service.findById(99L)).isEmpty();
    }

    @Test
    void findByAuthor_shouldDelegateToRepository() {
        service.findByAuthor("Bloch");
        verify(repository).findByAuthor("Bloch");
    }
}