package kt7_step_hybrid.support;

import ru.student.testing.model.Book;

public class TestExternalCatalog {

    private final boolean available;

    public TestExternalCatalog(boolean available) {
        this.available = available;
    }

    /** Пытается обогатить книгу внешними данными. */
    public Book enrich(Book book) {
        if (!available) {
            throw new IllegalStateException("External catalog unavailable");
        }
        return book;
    }
}