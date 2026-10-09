package kt6_step_reverse.step3;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.student.testing.LibraryApplication;
import ru.student.testing.controller.BookController;
import ru.student.testing.model.Book;
import ru.student.testing.repository.BookRepository;
import ru.student.testing.service.BookService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ContextConfiguration(classes = LibraryApplication.class)
@WebMvcTest(BookController.class)
class BookControllerBottomUpTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BookRepository bookRepository;   // репозиторий мокается
    @MockBean BookService bookService;         // сервис мокается

    @Test
    void getAll_shouldReturnJsonList() throws Exception {
        when(bookService.findAll()).thenReturn(List.of(
                new Book("Clean Code", "R. Martin", 2008)
        ));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].year").value(2008));
    }

    @Test
    void getById_shouldReturn404WhenMissing() throws Exception {
        when(bookService.findById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn400ForInvalidYear() throws Exception {
        Book invalid = new Book("Bad", "A", -5);
        when(bookService.add(any())).thenThrow(new IllegalArgumentException());

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnSavedBook() throws Exception {
        Book saved = new Book("Refactoring", "M. Fowler", 1999);
        saved.setId(1L);
        when(bookService.add(any())).thenReturn(saved);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Book("Refactoring", "M. Fowler", 1999))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.year").value(1999));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).delete(1L);
    }

    @Test
    void byAuthor_shouldPassParamToService() throws Exception {
        when(bookService.findByAuthor("Fowler")).thenReturn(List.of());

        mockMvc.perform(get("/api/books/by-author").param("author", "Fowler"))
                .andExpect(status().isOk());

        verify(bookService).findByAuthor("Fowler");
    }
}