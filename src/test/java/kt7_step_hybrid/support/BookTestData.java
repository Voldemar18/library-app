package kt7_step_hybrid.support;

import ru.student.testing.model.Book;

public final class BookTestData {

    private BookTestData() {}

    public static Book cleanCode() {
        return new Book("Clean Code", "Robert C. Martin", 2008);
    }

    public static Book effectiveJava() {
        return new Book("Effective Java", "Joshua Bloch", 2018);
    }

    public static Book refactoring() {
        return new Book("Refactoring", "Martin Fowler", 1999);
    }

    public static Book invalidYear() {
        return new Book("Bad Book", "Unknown", -5);
    }

    public static Book futureBook() {
        return new Book("From the Future", "AI", 3000);
    }
}
