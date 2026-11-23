package ru.tccenter.ciiconfigdiff.service.internal;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
class CiiConfigHashService {

    private final JsonUtils jsonUtils;

    /**
     * Вычисляет SHA-256 хеш канонического представления JSON-узла.
     *
     * @param node JSON-узел для хеширования
     * @return hex-строка SHA-256 хеша
     */
    String sha256(JsonNode node) {
        String canonical = jsonUtils.toCanonicalJson(node);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
