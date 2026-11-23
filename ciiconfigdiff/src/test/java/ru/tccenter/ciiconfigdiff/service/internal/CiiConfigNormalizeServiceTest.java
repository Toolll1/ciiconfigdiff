package ru.tccenter.ciiconfigdiff.service.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CiiConfigNormalizeServiceTest {

    private final CiiConfigNormalizeService service = new CiiConfigNormalizeService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void removesServiceFieldsAndKeepsBusinessFields() throws Exception {
        JsonNode payload = mapper.readTree("""
                {
                  "id": 71,
                  "version": 1,
                  "versionCounter": 1,
                  "status": "OFF",
                  "ciiCode": "TestVG_GVDB.1.1.3.2",
                  "ciiGroup": "TestVG.1.1.X.2",
                  "integrationType": "JDBC_SINK"
                }
                """);

        JsonNode normalized = service.removeServiceFields(payload);

        assertThat(normalized.has("id")).isFalse();
        assertThat(normalized.has("version")).isFalse();
        assertThat(normalized.has("versionCounter")).isFalse();
        assertThat(normalized.has("status")).isFalse();
        assertThat(normalized.get("ciiCode").asText()).isEqualTo("TestVG_GVDB.1.1.3.2");
        assertThat(normalized.get("ciiGroup").asText()).isEqualTo("TestVG.1.1.X.2");
        assertThat(normalized.get("integrationType").asText()).isEqualTo("JDBC_SINK");
        assertThat(payload.has("id")).isTrue();
    }
}
