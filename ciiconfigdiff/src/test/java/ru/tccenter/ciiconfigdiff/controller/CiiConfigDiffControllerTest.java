package ru.tccenter.ciiconfigdiff.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.tccenter.ciiconfigdiff.service.api.CiiConfigDiffService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CiiConfigDiffController.class)
@AutoConfigureMockMvc
class CiiConfigDiffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CiiConfigDiffService ciiConfigDiffService;

    @Test
    void returnsHtmlDiffReport() throws Exception {
        when(ciiConfigDiffService.diff()).thenReturn("<table");

        mockMvc.perform(get("/api/cii-config/diff"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("<table")));
    }
}