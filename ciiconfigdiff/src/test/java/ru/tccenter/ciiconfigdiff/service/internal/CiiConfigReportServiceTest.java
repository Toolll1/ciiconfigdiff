package ru.tccenter.ciiconfigdiff.service.internal;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CiiConfigReportServiceTest {

    private final CiiConfigReportService service = new CiiConfigReportService();

    @Test
    void rendersNoDifferencesMessage() {
        CiiConfigDiffResult result = CiiConfigDiffResult.builder()
                .ciiCode("CODE")
                .ciiGroup("GROUP")
                .fieldDiffs(List.of())
                .build();

        String html = service.toHtml(List.of(result));

        assertThat(html).contains("<h1>CII Config Diff</h1>");
        assertThat(html).contains("Различий нет");
        assertThat(html).doesNotContain("<table>");
    }

    @Test
    void rendersDifferenceTableWithBordersAndMissingValuePlaceholder() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("dev", "500");
        values.put("prod", null);

        FieldDiff diff = FieldDiff.builder()
                .fieldPath("integrationProperties.kafkaconnect.batch.size")
                .valuesByEnv(values)
                .missingInSomeEnv(true)
                .build();

        CiiConfigDiffResult result = CiiConfigDiffResult.builder()
                .ciiCode("CODE")
                .ciiGroup("GROUP")
                .fieldDiffs(List.of(diff))
                .build();

        String html = service.toHtml(List.of(result));

        assertThat(html).contains("border-collapse: collapse");
        assertThat(html).contains("border: 1px solid");
        assertThat(html).contains("<th>Поле</th>");
        assertThat(html).contains("<th>dev</th>");
        assertThat(html).contains("<th>tifa</th>");
        assertThat(html).contains("<th>tli</th>");
        assertThat(html).contains("<th>tfi</th>");
        assertThat(html).contains("<th>prod</th>");
        assertThat(html).contains("integrationProperties.kafkaconnect.batch.size");
        assertThat(html).contains("<td>500</td>");
        assertThat(html).contains("<td>—</td>");
        assertThat(html).doesNotContain("Различий нет");
    }
}
