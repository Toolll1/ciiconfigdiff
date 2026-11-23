package ru.tccenter.ciiconfigdiff.service.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CiiConfigDiffResult {

    private String ciiCode;
    private String ciiGroup;
    @Builder.Default
    private List<FieldDiff> fieldDiffs = new ArrayList<>();

    /**
     * Проверяет, есть ли различия в группе конфигураций.
     *
     * @return {@code true}, если список различий не пуст
     */
    boolean hasDifferences() {
        return fieldDiffs != null && !fieldDiffs.isEmpty();
    }
}