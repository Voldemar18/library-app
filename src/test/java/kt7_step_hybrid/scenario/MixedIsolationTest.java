package kt7_step_hybrid.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt7_step_hybrid.support.BookTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.student.testing.LibraryApplication;
import ru.student.testing.controller.BookController;
import ru.student.testing.model.Book;
import ru.student.testing.repository.BookRepository;
import ru.student.testing.service.BookService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ContextConfiguration(classes = LibraryApplication.class)
@WebMvcTest(BookController.class)
@Import(BookService.class)
class MixedIsolationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper json;

    @MockBean BookRepository repository;   // репозиторий мокается

    @Test
    @DisplayName("Невалидный год → 400, в репозиторий ничего не уходит")
    void invalidYear_rejectedBeforeRepository() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(BookTestData.invalidYear())))
                .andExpect(status().isBadRequest());

        Mockito.verify(repository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Граница диапазона: год 0 принимается")
    void yearZero_accepted() throws Exception {
        Book saved = new Book("Zero", "A", 0);
        saved.setId(1L);
        when(repository.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Book("Zero", "A", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(0));
    }

    @Test
    @DisplayName("Граница диапазона: год 2100 принимается")
    void yearMax_accepted() throws Exception {
        Book saved = new Book("Max", "A", 2100);
        saved.setId(1L);
        when(repository.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Book("Max", "A", 2100))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Поиск по id, которого нет, отдаёт 404 без обращения к save")
    void missingId_returns404() throws Exception {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/404"))
                .andExpect(status().isNotFound());
    }
}
