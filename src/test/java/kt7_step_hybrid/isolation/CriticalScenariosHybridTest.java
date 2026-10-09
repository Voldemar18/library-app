package kt7_step_hybrid.isolation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kt7_step_hybrid.support.BookTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.student.testing.LibraryApplication;
import ru.student.testing.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ContextConfiguration(classes = LibraryApplication.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CriticalScenariosHybridTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper json;
    @Autowired BookRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Библиотекарь добавляет книгу → она появляется в каталоге")
    void addBook_thenVisibleInCatalog() throws Exception {
        // 1. POST — добавляем книгу через HTTP
        String response = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(BookTestData.cleanCode())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        Long id = json.readTree(response).get("id").asLong();

        // 2. GET — книга видна в общем списке
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));

        // 3. GET по id — карточка книги
        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Robert C. Martin"));

        // 4. Проверка, что данные реально в БД (а не только в ответе)
        assertThat(repository.findById(id)).isPresent();
    }

    @Test
    @DisplayName("Поиск по автору возвращает только его книги")
    void searchByAuthor() throws Exception {
        repository.save(BookTestData.cleanCode());      // Martin
        repository.save(BookTestData.effectiveJava());  // Bloch
        repository.save(BookTestData.refactoring());    // Fowler

        mockMvc.perform(get("/api/books/by-author").param("author", "Joshua Bloch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Effective Java"));
    }

    @Test
    @DisplayName("Удаление книги убирает её из каталога")
    void deleteBook_removesFromCatalog() throws Exception {
        Long id = repository.save(BookTestData.cleanCode()).getId();

        mockMvc.perform(delete("/api/books/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isNotFound());

        assertThat(repository.findById(id)).isEmpty();
    }
}