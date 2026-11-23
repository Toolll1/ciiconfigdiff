package ru.tccenter.ciiconfigdiff.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import ru.tccenter.ciiconfigdiff.model.CiiConfigDiffResult;
import ru.tccenter.ciiconfigdiff.model.CiiConfigDto;
import ru.tccenter.ciiconfigdiff.model.FieldDiff;
import ru.tccenter.ciiconfigdiff.util.JsonUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class CiiConfigCompareService {

    private static final String MISSING_ENV_VALUE = "none";

    /**
     * Сравнивает конфигурации всех контуров и формирует результаты по группам CII.
     *
     * @param configs список конфигураций для сравнения
     * @return список результатов сравнения
     */
    public List<CiiConfigDiffResult> compare(List<CiiConfigDto> configs) {
        Map<String, List<CiiConfigDto>> groups = groupByCiiKey(configs);
        List<CiiConfigDiffResult> results = new ArrayList<>();
        for (List<CiiConfigDto> group : groups.values()) {
            results.add(toDiffResult(group));
        }
        return results;
    }

    /**
     * Группирует конфигурации по ключу {@code ciiCode|ciiGroup}.
     *
     * @param configs список конфигураций
     * @return группы конфигураций в порядке появления
     */
    private Map<String, List<CiiConfigDto>> groupByCiiKey(List<CiiConfigDto> configs) {
        return configs.stream()
                .collect(Collectors.groupingBy(this::groupKey, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * Преобразует группу конфигураций в результат сравнения.
     *
     * @param group группа конфигураций одного CII
     * @return результат сравнения группы
     */
    private CiiConfigDiffResult toDiffResult(List<CiiConfigDto> group) {
        JsonNode sampleNode = group.getFirst().getNormalized();
        return CiiConfigDiffResult.builder()
                .ciiCode(JsonUtils.textOrNull(sampleNode, "ciiCode"))
                .ciiGroup(JsonUtils.textOrNull(sampleNode, "ciiGroup"))
                .fieldDiffs(diffGroup(group))
                .build();
    }

    /**
     * Вычисляет различия полей внутри группы конфигураций.
     *
     * @param group группа конфигураций одного CII
     * @return список различий полей
     */
    private List<FieldDiff> diffGroup(List<CiiConfigDto> group) {
        List<String> missingEnvs = findMissingEnvironments(group);
        if (!hasDifferences(group, missingEnvs)) {
            return List.of();
        }

        Map<String, List<String>> fieldPaths = collectAllFieldPaths(group);
        return buildFieldDiffs(group, missingEnvs, fieldPaths);
    }

    /**
     * Определяет, нужно ли строить различия для группы.
     *
     * @param group       группа конфигураций
     * @param missingEnvs контуры, где конфигурация отсутствует
     * @return {@code true}, если есть различия или отсутствующие контуры
     */
    private boolean hasDifferences(List<CiiConfigDto> group, List<String> missingEnvs) {
        if (!missingEnvs.isEmpty()) {
            return true;
        }
        Set<String> hashes = group.stream().map(CiiConfigDto::getHash).collect(Collectors.toSet());
        return hashes.size() > 1;
    }

    /**
     * Находит контуры, для которых отсутствует конфигурация в группе.
     *
     * @param group группа конфигураций
     * @return список отсутствующих контуров
     */
    private List<String> findMissingEnvironments(List<CiiConfigDto> group) {
        Set<String> presentEnvs = group.stream()
                .map(CiiConfigDto::getEnvironment)
                .collect(Collectors.toSet());
        return CiiConfigFetchService.ENVIRONMENTS.stream()
                .filter(env -> !presentEnvs.contains(env))
                .toList();
    }

    /**
     * Собирает все пути полей из всех конфигураций группы.
     *
     * @param group группа конфигураций
     * @return отображение пути поля на сегменты пути
     */
    private Map<String, List<String>> collectAllFieldPaths(List<CiiConfigDto> group) {
        Map<String, List<String>> fieldPaths = new TreeMap<>();
        for (CiiConfigDto config : group) {
            collectFieldPaths(config.getNormalized(), List.of(), fieldPaths);
        }
        return fieldPaths;
    }

    /**
     * Формирует различия по всем собранным путям полей.
     *
     * @param group      группа конфигураций
     * @param missingEnvs контуры без конфигурации
     * @param fieldPaths пути полей для сравнения
     * @return список различий полей
     */
    private List<FieldDiff> buildFieldDiffs(List<CiiConfigDto> group,
                                            List<String> missingEnvs,
                                            Map<String, List<String>> fieldPaths) {
        List<FieldDiff> diffs = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : fieldPaths.entrySet()) {
            FieldDiff diff = buildFieldDiff(group, missingEnvs, entry.getKey(), entry.getValue());
            if (hasDistinctValues(diff)) {
                diffs.add(diff);
            }
        }
        return diffs;
    }

    /**
     * Формирует различие для одного поля.
     *
     * @param group      группа конфигураций
     * @param missingEnvs контуры без конфигурации
     * @param fieldPath  отображаемый путь поля
     * @param segments   сегменты пути для доступа к значению
     * @return различие поля
     */
    private FieldDiff buildFieldDiff(List<CiiConfigDto> group,
                                     List<String> missingEnvs,
                                     String fieldPath,
                                     List<String> segments) {
        Map<String, String> valuesByEnv = new LinkedHashMap<>();
        boolean missingInSomeEnv = collectPresentValues(group, segments, valuesByEnv);
        missingInSomeEnv |= addMissingEnvironmentValues(missingEnvs, valuesByEnv);
        return FieldDiff.builder()
                .fieldPath(fieldPath)
                .valuesByEnv(valuesByEnv)
                .missingInSomeEnv(missingInSomeEnv)
                .build();
    }

    /**
     * Собирает значения поля из конфигураций присутствующих контуров.
     *
     * @param group       группа конфигураций
     * @param segments    сегменты пути поля
     * @param valuesByEnv накопитель значений по контурам
     * @return {@code true}, если значение отсутствует хотя бы в одном присутствующем контуре
     */
    private boolean collectPresentValues(List<CiiConfigDto> group,
                                         List<String> segments,
                                         Map<String, String> valuesByEnv) {
        boolean missingInSomeEnv = false;
        for (CiiConfigDto config : group) {
            JsonNode value = resolvePath(config.getNormalized(), segments);
            if (isMissing(value)) {
                missingInSomeEnv = true;
                valuesByEnv.put(config.getEnvironment(), null);
            } else {
                valuesByEnv.put(config.getEnvironment(), JsonUtils.toDisplayValue(value));
            }
        }
        return missingInSomeEnv;
    }

    /**
     * Добавляет значения {@code none} для контуров без конфигурации.
     *
     * @param missingEnvs контуры без конфигурации
     * @param valuesByEnv накопитель значений по контурам
     * @return {@code true}, если были добавлены отсутствующие контуры
     */
    private boolean addMissingEnvironmentValues(List<String> missingEnvs, Map<String, String> valuesByEnv) {
        for (String env : missingEnvs) {
            valuesByEnv.put(env, MISSING_ENV_VALUE);
        }
        return !missingEnvs.isEmpty();
    }

    /**
     * Проверяет, содержит ли различие поля разные значения.
     *
     * @param diff различие поля
     * @return {@code true}, если значения различаются
     */
    private boolean hasDistinctValues(FieldDiff diff) {
        Set<String> distinctValues = new LinkedHashSet<>(diff.getValuesByEnv().values());
        return distinctValues.size() > 1;
    }

    /**
     * Проверяет, является ли JSON-значение отсутствующим.
     *
     * @param value JSON-значение
     * @return {@code true}, если значение отсутствует или равно null
     */
    private boolean isMissing(JsonNode value) {
        return value == null || value.isMissingNode() || value.isNull();
    }

    /**
     * Формирует ключ группы сравнения по коду и группе CII.
     *
     * @param config конфигурация
     * @return строковый ключ группы
     */
    private String groupKey(CiiConfigDto config) {
        JsonNode node = config.getNormalized();
        return Objects.toString(JsonUtils.textOrNull(node, "ciiCode"), "")
                + "|"
                + Objects.toString(JsonUtils.textOrNull(node, "ciiGroup"), "");
    }

    /**
     * Рекурсивно собирает пути всех полей JSON-объекта.
     *
     * @param node   текущий JSON-узел
     * @param prefix текущие сегменты пути
     * @param paths  накопитель путей полей
     */
    void collectFieldPaths(JsonNode node, List<String> prefix, Map<String, List<String>> paths) {
        if (node == null || !node.isObject()) {
            addLeafPath(prefix, paths);
            return;
        }
        node.fields().forEachRemaining(field -> collectField(prefix, field.getKey(), field.getValue(), paths));
    }

    /**
     * Обрабатывает одно поле JSON-объекта и добавляет его путь в накопитель.
     *
     * @param prefix текущие сегменты пути
     * @param name   имя поля
     * @param value  значение поля
     * @param paths  накопитель путей полей
     */
    private void collectField(List<String> prefix, String name, JsonNode value, Map<String, List<String>> paths) {
        List<String> path = appendSegment(prefix, name);
        if (isNestedObject(value)) {
            collectFieldPaths(value, path, paths);
            return;
        }
        paths.put(String.join(".", path), List.copyOf(path));
    }

    /**
     * Создаёт новый путь, добавляя сегмент к текущему префиксу.
     *
     * @param prefix текущие сегменты пути
     * @param name   добавляемый сегмент
     * @return новый список сегментов пути
     */
    private List<String> appendSegment(List<String> prefix, String name) {
        List<String> path = new ArrayList<>(prefix);
        path.add(name);
        return path;
    }

    /**
     * Проверяет, нужно ли рекурсивно обходить значение как вложенный объект.
     *
     * @param value JSON-значение
     * @return {@code true}, если значение является непустым объектом
     */
    private boolean isNestedObject(JsonNode value) {
        return value != null && value.isObject() && !value.isEmpty();
    }

    /**
     * Добавляет конечный путь поля в накопитель, если префикс не пустой.
     *
     * @param prefix сегменты пути
     * @param paths  накопитель путей полей
     */
    private void addLeafPath(List<String> prefix, Map<String, List<String>> paths) {
        if (!prefix.isEmpty()) {
            paths.put(String.join(".", prefix), List.copyOf(prefix));
        }
    }

    /**
     * Получает значение JSON-узла по списку сегментов пути.
     *
     * @param node     исходный JSON-узел
     * @param segments сегменты пути
     * @return найденный узел или {@code null}, если путь недоступен
     */
    private JsonNode resolvePath(JsonNode node, List<String> segments) {
        JsonNode current = node;
        for (String segment : segments) {
            if (current == null || !current.isObject()) {
                return null;
            }
            current = current.get(segment);
        }
        return current;
    }
}
