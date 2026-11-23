package ru.tccenter.ciiconfigdiff.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CiiConfigDiffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Проверяет, что эндпоинт возвращает HTML-отчёт с таблицей различий и стилями границ.
     */
    @Test
    void returnsHtmlDiffReport() throws Exception {
        mockMvc.perform(get("/api/cii-config/diff"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("CII Config Diff")))
                .andExpect(content().string(containsString("<table")))
                .andExpect(content().string(containsString("border-collapse: collapse")));
    }
}
