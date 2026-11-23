package ru.tccenter.ciiconfigdiff.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import ru.tccenter.ciiconfigdiff.util.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class CiiConfigHashService {

    /**
     * Вычисляет SHA-256 хеш канонического представления JSON-узла.
     *
     * @param node JSON-узел для хеширования
     * @return hex-строка SHA-256 хеша
     */
    public String sha256(JsonNode node) {
        String canonical = JsonUtils.toCanonicalJson(node);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
