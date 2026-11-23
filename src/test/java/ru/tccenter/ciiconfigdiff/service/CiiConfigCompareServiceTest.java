package ru.tccenter.ciiconfigdiff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import ru.tccenter.ciiconfigdiff.model.CiiConfigDiffResult;
import ru.tccenter.ciiconfigdiff.model.CiiConfigDto;
import ru.tccenter.ciiconfigdiff.model.FieldDiff;

import java.util.List;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class CiiConfigCompareServiceTest {

    private final CiiConfigCompareService compareService = new CiiConfigCompareService();
    private final CiiConfigHashService hashService = new CiiConfigHashService();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Проверяет, что одинаковые хеши не создают различий.
     */
    @Test
    void identicalHashesProduceNoDiffs() throws Exception {
        JsonNode node = mapper.readTree("""
                {
                  "ciiCode": "CODE",
                  "ciiGroup": "GROUP",
                  "sourceSystem": "KAFKA",
                  "integrationProperties": {"kafkaconnect.batch.size": "500"}
                }
                """);

        List<CiiConfigDiffResult> results = compareService.compare(List.of(
                dto("dev", node),
                dto("prod", node.deepCopy())
        ));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().hasDifferences()).isFalse();
    }

    /**
     * Проверяет поиск различий на верхнем уровне, во вложенных объектах и отсутствие полей.
     */
    @Test
    void findsTopLevelAndNestedDifferencesAndMissingFields() throws Exception {
        ObjectNode dev = (ObjectNode) mapper.readTree("""
                {
                  "ciiCode": "CODE",
                  "ciiGroup": "GROUP",
                  "sourceSystem": "KAFKA",
                  "integrationProperties": {"kafkaconnect.batch.size": "500"}
                }
                """);
        ObjectNode prod = dev.deepCopy();
        prod.put("sourceSystem", "FILE");
        ((ObjectNode) prod.get("integrationProperties")).put("kafkaconnect.batch.size", "100");
        ((ObjectNode) prod.get("integrationProperties")).put("kafkaconnect.tasks.max", "1");

        List<CiiConfigDiffResult> results = compareService.compare(List.of(
                dto("dev", dev),
                dto("prod", prod)
        ));

        List<FieldDiff> diffs = results.getFirst().getFieldDiffs();
        assertThat(diffs).extracting(FieldDiff::getFieldPath)
                .contains(
                        "integrationProperties.kafkaconnect.batch.size",
                        "integrationProperties.kafkaconnect.tasks.max",
                        "sourceSystem"
                );

        FieldDiff nested = findDiff(diffs, "integrationProperties.kafkaconnect.batch.size");
        assertThat(nested.getValuesByEnv().get("dev")).isEqualTo("500");
        assertThat(nested.getValuesByEnv().get("prod")).isEqualTo("100");
        assertThat(nested.isMissingInSomeEnv()).isFalse();

        FieldDiff missing = findDiff(diffs, "integrationProperties.kafkaconnect.tasks.max");
        assertThat(missing.isMissingInSomeEnv()).isTrue();
        assertThat(missing.getValuesByEnv().get("dev")).isNull();
        assertThat(missing.getValuesByEnv().get("prod")).isEqualTo("1");
    }

    /**
     * Проверяет, что объекты с разными ключами CII не сравниваются между собой.
     */
    @Test
    void doesNotCompareObjectsWithDifferentKeys() throws Exception {
        JsonNode first = mapper.readTree("""
                {"ciiCode":"A","ciiGroup":"G1","sourceSystem":"KAFKA"}
                """);
        JsonNode second = mapper.readTree("""
                {"ciiCode":"A","ciiGroup":"G2","sourceSystem":"FILE"}
                """);

        List<CiiConfigDiffResult> results = compareService.compare(List.of(
                dto("dev", first),
                dto("prod", second)
        ));

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(result -> !result.hasDifferences());
    }

    /**
     * Проверяет сбор путей для произвольных вложенных полей без жёстко заданных имён.
     */
    @Test
    void collectsArbitraryNestedFieldsWithoutHardcodedNames() throws Exception {
        JsonNode node = mapper.readTree("""
                {
                  "custom": {"inner": "value", "deep": {"leaf": 1}},
                  "integrationProperties": {"a": "b"}
                }
                """);

        var paths = new TreeMap<String, List<String>>();
        compareService.collectFieldPaths(node, List.of(), paths);

        assertThat(paths.keySet()).contains(
                "custom.inner",
                "custom.deep.leaf",
                "integrationProperties.a"
        );
    }

    /**
     * Проверяет, что отсутствие конфигурации на контуре отображается как {@code none} для всех полей.
     */
    @Test
    void missingEnvironmentConfigIsReportedAsNoneForAllFields() throws Exception {
        JsonNode node = mapper.readTree("""
                {
                  "ciiCode": "CODE",
                  "ciiGroup": "GROUP",
                  "sourceSystem": "KAFKA"
                }
                """);

        List<CiiConfigDiffResult> results = compareService.compare(List.of(
                dto("dev", node),
                dto("prod", node.deepCopy())
        ));

        List<FieldDiff> diffs = results.getFirst().getFieldDiffs();
        assertThat(diffs).isNotEmpty();
        assertThat(diffs).allSatisfy(diff -> {
            assertThat(diff.getValuesByEnv().get("tifa")).isEqualTo("none");
            assertThat(diff.getValuesByEnv().get("tli")).isEqualTo("none");
            assertThat(diff.getValuesByEnv().get("tfi")).isEqualTo("none");
            assertThat(diff.getValuesByEnv().get("dev")).isNotEqualTo("none");
        });
    }

    /**
     * Находит различие поля по его пути.
     *
     * @param diffs     список различий
     * @param fieldPath путь поля
     * @return найденное различие
     */
    private FieldDiff findDiff(List<FieldDiff> diffs, String fieldPath) {
        return diffs.stream()
                .filter(diff -> diff.getFieldPath().equals(fieldPath))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Создаёт DTO конфигурации для тестов.
     *
     * @param env        имя контура
     * @param normalized нормализованный JSON
     * @return подготовленный DTO конфигурации
     */
    private CiiConfigDto dto(String env, JsonNode normalized) {
        return CiiConfigDto.builder()
                .environment(env)
                .normalized(normalized)
                .hash(hashService.sha256(normalized))
                .build();
    }
}
