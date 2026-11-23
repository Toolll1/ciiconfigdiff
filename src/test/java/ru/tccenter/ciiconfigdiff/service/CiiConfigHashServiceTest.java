package ru.tccenter.ciiconfigdiff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CiiConfigHashServiceTest {

    private final CiiConfigHashService service = new CiiConfigHashService();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Проверяет, что одинаковое содержимое даёт одинаковый хеш независимо от порядка ключей.
     */
    @Test
    void sameContentProducesSameHashRegardlessOfKeyOrder() throws Exception {
        JsonNode first = mapper.readTree("""
                {"ciiCode":"A","ciiGroup":"G","active":false}
                """);
        JsonNode second = mapper.readTree("""
                {"active":false,"ciiGroup":"G","ciiCode":"A"}
                """);

        assertThat(service.sha256(first)).isEqualTo(service.sha256(second));
    }

    /**
     * Проверяет, что разное содержимое даёт разные хеши.
     */
    @Test
    void differentContentProducesDifferentHash() throws Exception {
        JsonNode first = mapper.readTree("""
                {"ciiCode":"A","ciiGroup":"G"}
                """);
        JsonNode second = mapper.readTree("""
                {"ciiCode":"B","ciiGroup":"G"}
                """);

        assertThat(service.sha256(first)).isNotEqualTo(service.sha256(second));
    }
}
