package ru.tccenter.ciiconfigdiff.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CiiConfigDto {

    private String environment;
    private JsonNode payload;
    private JsonNode normalized;
    private String hash;
}
