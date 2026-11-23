package ru.tccenter.ciiconfigdiff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tccenter.ciiconfigdiff.service.api.CiiConfigDiffService;

@RestController
@RequiredArgsConstructor
public class CiiConfigDiffController {

    private final CiiConfigDiffService ciiConfigDiffService;


    /**
     * Формирует HTML-отчёт о различиях конфигураций CII между контурами.
     *
     * @return HTML-страница с таблицами различий
     */
    @GetMapping(value = "/api/cii-config/diff", produces = MediaType.TEXT_HTML_VALUE)
    public String diff() {
        return ciiConfigDiffService.diff();
    }
}