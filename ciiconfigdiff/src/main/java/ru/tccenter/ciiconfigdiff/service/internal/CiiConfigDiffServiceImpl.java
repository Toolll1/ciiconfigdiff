package ru.tccenter.ciiconfigdiff.service.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tccenter.ciiconfigdiff.service.api.CiiConfigDiffService;

import java.util.List;

@Service
@RequiredArgsConstructor
class CiiConfigDiffServiceImpl implements CiiConfigDiffService {

    private final CiiConfigFetchService fetchService;
    private final CiiConfigNormalizeService normalizeService;
    private final CiiConfigHashService hashService;
    private final CiiConfigCompareService compareService;
    private final CiiConfigReportService reportService;

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