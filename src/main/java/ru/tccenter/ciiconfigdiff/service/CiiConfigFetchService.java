package ru.tccenter.ciiconfigdiff.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.tccenter.ciiconfigdiff.model.CiiConfigDto;
import ru.tccenter.ciiconfigdiff.util.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class CiiConfigFetchService {

    public static final List<String> ENVIRONMENTS = List.of("dev", "tifa", "tli", "tfi", "prod");

    private static final String STUB_RESOURCE = "stub/cii-config.json";
    private static final String DEV_STUB_RESOURCE = "stub/cii-config_other.json";
    private static final String STUB_HTTP_URL = "http://stub/cii-configs";

    private final RestClient restClient;

    /**
     * Создаёт сервис загрузки конфигураций и инициализирует HTTP-клиент.
     *
     * @param restClientBuilder билдер для создания {@link RestClient}
     */
    public CiiConfigFetchService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    /**
     * Загружает конфигурации всех контуров из stub-ресурсов.
     *
     * @return список конфигураций всех контуров
     */
    public List<CiiConfigDto> fetchAll() {
        List<CiiConfigDto> configs = new ArrayList<>();
        for (String environment : ENVIRONMENTS) {
            configs.addAll(fetchEnvironmentConfigs(environment));
        }
        return configs;
    }

    /**
     * Загружает список конфигураций по HTTP. Метод-заглушка, пока не используется в основном процессе.
     *
     * @return JSON-массив конфигураций, полученный по HTTP
     */
    public JsonNode fetchFromHttp() {
        return restClient.get()
                .uri(STUB_HTTP_URL)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Загружает и преобразует конфигурации одного контура.
     *
     * @param environment имя контура
     * @return список DTO конфигураций контура
     */
    private List<CiiConfigDto> fetchEnvironmentConfigs(String environment) {
        JsonNode payloads = loadStubPayload(environment);
        if (payloads == null || !payloads.isArray()) {
            throw new IllegalArgumentException("CII config source must be a JSON array: " + environment);
        }
        return toConfigDtos(environment, payloads);
    }

    /**
     * Выбирает stub-ресурс для контура и загружает его содержимое.
     *
     * @param environment имя контура
     * @return JSON-массив конфигураций из ресурса
     */
    private JsonNode loadStubPayload(String environment) {
        String resourcePath = "dev".equals(environment) ? DEV_STUB_RESOURCE : STUB_RESOURCE;
        return loadResource(resourcePath);
    }

    /**
     * Преобразует JSON-массив конфигураций в список DTO.
     *
     * @param environment имя контура
     * @param payloads    JSON-массив конфигураций
     * @return список DTO конфигураций
     */
    private List<CiiConfigDto> toConfigDtos(String environment, JsonNode payloads) {
        List<CiiConfigDto> configs = new ArrayList<>();
        for (JsonNode payload : payloads) {
            configs.add(CiiConfigDto.builder()
                    .environment(environment)
                    .payload(payload)
                    .build());
        }
        return configs;
    }

    /**
     * Читает JSON-ресурс из classpath.
     *
     * @param path путь к ресурсу в classpath
     * @return прочитанный JSON-узел
     */
    private JsonNode loadResource(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return JsonUtils.mapper().readTree(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load stub CII config: " + path, e);
        }
    }
}
