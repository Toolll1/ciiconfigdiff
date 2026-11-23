package ru.tccenter.ciiconfigdiff.service.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
class JsonUtils {

    private final ObjectMapper objectMapper;

    JsonNode sortKeys(JsonNode node) {
        if (node == null || !node.isObject()) {
            return node;
        }
        ObjectNode sorted = objectMapper.createObjectNode();
        Map<String, JsonNode> entries = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            entries.put(field.getKey(), sortKeys(field.getValue()));
        }
        entries.forEach(sorted::set);
        return sorted;
    }

    String toCanonicalJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(sortKeys(node));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }

    String toDisplayValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isValueNode()) {
            return node.asText();
        }
        try {
            return objectMapper.writeValueAsString(sortKeys(node));
        } catch (Exception e) {
            return node.toString();
        }
    }

    String textOrNull(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }
}
