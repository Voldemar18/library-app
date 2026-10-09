package step4;

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
class FullStackIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired
    BookRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void endToEnd_createReadDelete() throws Exception {
        Book book = new Book("Refactoring", "M. Fowler", 1999);

        String response = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactoring"));

        assertThat(repository.findById(id)).isPresent();

        mockMvc.perform(delete("/api/books/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void endToEnd_filterByAuthor() throws Exception {
        repository.save(new Book("B1", "AuthorA", 2000));
        repository.save(new Book("B2", "AuthorB", 2001));

        mockMvc.perform(get("/api/books/by-author").param("author", "AuthorA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("B1"));
    }
}
