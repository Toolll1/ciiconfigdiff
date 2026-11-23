package ru.tccenter.ciiconfigdiff.service.api;

public interface CiiConfigDiffService {


    /**
     * Формирует HTML-отчёт о различиях конфигураций CII между контурами.
     *
     * @return HTML-страница с таблицами различий
     */
    String diff();
}
