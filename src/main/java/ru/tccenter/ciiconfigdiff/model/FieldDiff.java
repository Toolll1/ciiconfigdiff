package ru.tccenter.ciiconfigdiff.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDiff {

    private String fieldPath;
    private Map<String, String> valuesByEnv;
    private boolean missingInSomeEnv;
}
