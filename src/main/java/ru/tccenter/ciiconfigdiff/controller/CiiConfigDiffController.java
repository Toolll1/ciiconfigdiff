package ru.tccenter.ciiconfigdiff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tccenter.ciiconfigdiff.model.CiiConfigDiffResult;
import ru.tccenter.ciiconfigdiff.model.CiiConfigDto;
import ru.tccenter.ciiconfigdiff.service.CiiConfigCompareService;
import ru.tccenter.ciiconfigdiff.service.CiiConfigFetchService;
import ru.tccenter.ciiconfigdiff.service.CiiConfigHashService;
import ru.tccenter.ciiconfigdiff.service.CiiConfigNormalizeService;
import ru.tccenter.ciiconfigdiff.service.CiiConfigReportService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CiiConfigDiffController {

    private final CiiConfigFetchService fetchService;
    private final CiiConfigNormalizeService normalizeService;
    private final CiiConfigHashService hashService;
    private final CiiConfigCompareService compareService;
    private final CiiConfigReportService reportService;

    /**
     * Формирует HTML-отчёт о различиях конфигураций CII между контурами.
     *
     * @return HTML-страница с таблицами различий
     */
    @GetMapping(value = "/api/cii-config/diff", produces = MediaType.TEXT_HTML_VALUE)
    public String diff() {
        List<CiiConfigDto> configs = fetchAndPrepareConfigs();
        List<CiiConfigDiffResult> results = compareService.compare(configs);
        return reportService.toHtml(results);
    }

    /**
     * Загружает конфигурации и подготавливает их к сравнению.
     *
     * @return список подготовленных конфигураций
     */
    private List<CiiConfigDto> fetchAndPrepareConfigs() {
        List<CiiConfigDto> configs = fetchService.fetchAll();
        configs.forEach(this::prepareConfig);
        return configs;
    }

    /**
     * Нормализует конфигурацию и вычисляет её хеш.
     *
     * @param config конфигурация для подготовки
     */
    private void prepareConfig(CiiConfigDto config) {
        var normalized = normalizeService.removeServiceFields(config.getPayload());
        config.setNormalized(normalized);
        config.setHash(hashService.sha256(normalized));
    }
}
