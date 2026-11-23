package ru.tccenter.ciiconfigdiff.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    /**
     * Запрещает создание экземпляров утилитного класса.
     */
    private JsonUtils() {
    }

    /**
     * Возвращает общий экземпляр {@link ObjectMapper} для работы с JSON.
     *
     * @return настроенный маппер
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Рекурсивно сортирует ключи JSON-объекта по алфавиту.
     *
     * @param node исходный JSON-узел
     * @return узел с отсортированными ключами или исходный узел, если он не является объектом
     */
    public static JsonNode sortKeys(JsonNode node) {
        if (node == null || !node.isObject()) {
            return node;
        }
        ObjectNode sorted = MAPPER.createObjectNode();
        Map<String, JsonNode> entries = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            entries.put(field.getKey(), sortKeys(field.getValue()));
        }
        entries.forEach(sorted::set);
        return sorted;
    }

    /**
     * Преобразует JSON-узел в каноническую строку с отсортированными ключами.
     *
     * @param node JSON-узел для сериализации
     * @return каноническое строковое представление JSON
     */
    public static String toCanonicalJson(JsonNode node) {
        try {
            return MAPPER.writeValueAsString(sortKeys(node));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }

    /**
     * Преобразует JSON-узел в строку для отображения в отчёте.
     *
     * @param node JSON-узел
     * @return строковое представление значения или {@code null}, если значение отсутствует
     */
    public static String toDisplayValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isValueNode()) {
            return node.asText();
        }
        try {
            return MAPPER.writeValueAsString(sortKeys(node));
        } catch (Exception e) {
            return node.toString();
        }
    }

    /**
     * Возвращает текстовое значение поля JSON-объекта или {@code null}, если поле отсутствует.
     *
     * @param node  JSON-объект
     * @param field имя поля
     * @return текстовое значение поля или {@code null}
     */
    public static String textOrNull(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }
}
