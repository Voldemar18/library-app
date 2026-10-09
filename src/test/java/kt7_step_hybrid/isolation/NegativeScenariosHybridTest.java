package kt7_step_hybrid.isolation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class NegativeScenariosHybridTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper json;
    @Autowired BookRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Пустой запрос POST → сохраняется с null-полями (известное ограничение)")
    void emptyBody_savesWithNullFields() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Некорректный JSON → 400")
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET по несуществующему id → 404")
    void getMissing_returns404() throws Exception {
        mockMvc.perform(get("/api/books/12345"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Поиск по автору без параметра → 400")
    void byAuthor_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/api/books/by-author"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Отрицательный год через HTTP → 400, БД пуста")
    void negativeYear_overHttp_rejected() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                new ru.student.testing.model.Book("A", "B", -5))))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isZero();
    }
}
