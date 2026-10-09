package kt7_step_hybrid.isolation;

import kt7_step_hybrid.support.BookTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
@ContextConfiguration(classes = LibraryApplication.class)
@DataJpaTest
@Import(BookService.class)
@ActiveProfiles("test")
class ConcurrencyAndEdgeCasesTest {

    @Autowired BookService service;
    @Autowired BookRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Валидация года: -1 и 3000 отвергаются, БД не меняется")
    void invalidYears_doNotPersist() {
        assertThatThrownBy(() -> service.add(new Book("A", "B", -1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.add(new Book("A", "B", 3000)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("Массовая вставка 100 книг — все сохраняются, findByAuthor возвращает 100")
    void bulkInsert() {
        IntStream.range(0, 100).forEach(i ->
                service.add(new Book("Book " + i, "Prolific Author", 2000 + i % 20))
        );

        List<Book> all = service.findByAuthor("Prolific Author");

        assertThat(all).hasSize(100);
    }

    @Test
    @DisplayName("Обновление книги: заголовок меняется, id сохраняется")
    void updateBookTitle() {
        Book saved = service.add(BookTestData.cleanCode());
        Long id = saved.getId();

        saved.setTitle("Clean Code, 2nd Edition");
        repository.save(saved);

        Book reloaded = repository.findById(id).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Clean Code, 2nd Edition");
        assertThat(reloaded.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Удаление несуществующей книги не падает")
    void deleteNonExistent_isSilentNoOp() {
        assertThatCode(() -> service.delete(9999L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Поиск по несуществующему автору → пустой список, не null")
    void searchByUnknownAuthor_returnsEmpty() {
        service.add(BookTestData.cleanCode());

        List<Book> result = service.findByAuthor("Nobody");

        assertThat(result).isNotNull().isEmpty();
    }
}