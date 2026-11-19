package com.prodman.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PlatformMetricsService {

    private final RestClient restClient;
    private final String prometheusUrl;

    public PlatformMetricsService(@Value("${prometheus.url:http://prometheus:9090}") String prometheusUrl) {
        this.prometheusUrl = prometheusUrl;
        this.restClient = RestClient.builder()
                .baseUrl(prometheusUrl)
                .build();
    }

    /**
     * Возвращает первый элемент result Prometheus-запроса
     * в формате { metric: {...}, value: [ts, "value"] }.
     * Если запрос не удался или result пуст — возвращает null.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> query(String promql) {
        try {
            // Собираем URI заранее: queryParam сам закодирует {, }, " и т.д.,
            // а RestClient.uri(URI) не будет трактовать их как шаблон.
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(prometheusUrl)
                    .path("/api/v1/query")
                    .queryParam("query", promql)
                    .build()
                    .encode()
                    .toUri();

            Map<String, Object> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) return null;
            Object data = response.get("data");
            if (!(data instanceof Map<?, ?> dataMap)) return null;
            Object result = dataMap.get("result");
            if (!(result instanceof List<?> list) || list.isEmpty()) return null;
            Object first = list.get(0);
            if (!(first instanceof Map<?, ?>)) return null;
            return (Map<String, Object>) first;
        } catch (Exception e) {
            log.warn("Prometheus query failed for '{}': {}", promql, e.getMessage());
            return null;
        }
    }

    /** Достаёт числовое значение из результата Prometheus. */
    public Double value(Map<String, Object> result) {
        if (result == null) return null;
        Object value = result.get("value");
        if (!(value instanceof List<?> pair) || pair.size() < 2) return null;
        try {
            return Double.parseDouble(String.valueOf(pair.get(1)));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}