package ru.tccenter.ciiconfigdiff.service.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class CiiConfigNormalizeService {

    static final List<String> SERVICE_FIELDS = List.of("id", "version", "versionCounter", "status");

    /**
     * Удаляет служебные поля из конфигурации перед сравнением.
     *
     * @param payload исходная конфигурация
     * @return копия конфигурации без служебных полей
     */
    JsonNode removeServiceFields(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return payload;
        }
        if (!payload.isObject()) {
            throw new IllegalArgumentException("CII config must be a JSON object");
        }
        ObjectNode copy = payload.deepCopy();
        copy.remove(SERVICE_FIELDS);
        return copy;
    }
}
