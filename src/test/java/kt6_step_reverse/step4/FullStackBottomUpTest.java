package kt6_step_reverse.step4;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.student.testing.LibraryApplication;
import ru.student.testing.model.Book;
import ru.student.testing.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ContextConfiguration(classes = LibraryApplication.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FullStackBottomUpTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BookRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void crudLifecycle() throws Exception {
        // CREATE
        String response = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Book("Refactoring", "M. Fowler", 1999))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // READ
        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactoring"));

        // Проверка, что реально в БД
        assertThat(repository.findById(id)).isPresent();

        // DELETE
        mockMvc.perform(delete("/api/books/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void filterByAuthor_endToEnd() throws Exception {
        repository.save(new Book("B1", "AuthorA", 2000));
        repository.save(new Book("B2", "AuthorB", 2001));
        repository.save(new Book("B3", "AuthorA", 2002));

        mockMvc.perform(get("/api/books/by-author").param("author", "AuthorA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title").value(
                        org.hamcrest.Matchers.containsInAnyOrder("B1", "B3")));
    }

    @Test
    void invalidYear_shouldReturn400_andNothingSaved() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Book("Bad", "A", 3000))))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isZero();
    }
}
