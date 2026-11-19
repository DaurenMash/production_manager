package com.prodman.userservice.controller;

import com.prodman.userservice.service.PlatformMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/platform/metrics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class PlatformMetricsController {

    private final PlatformMetricsService metricsService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        // up — служебная метрика Prometheus, у неё label job (не application)
        var up = metricsService.query("sum(up{job!=\"api-gateway\"})");

        // RPS — все HTTP-запросы за 5 минут
        var rps = metricsService.query("sum(rate(http_server_requests_seconds_count[5m]))");

        // Ошибки — только 5xx
        var errors = metricsService.query(
                "sum(rate(http_server_requests_seconds_count{status=~\"5..\"}[5m]))");

        // DB connections — сумма по всем сервисам, у кого есть Hikari
        var dbConn = metricsService.query(
                "sum(hikaricp_connections_active{application!=\"api-gateway\"})");

        double upVal = safeValue(up);
        double rpsVal = safeValue(rps);
        double errorsVal = safeValue(errors);
        double dbVal = safeValue(dbConn);

        double errorRate = rpsVal > 0 ? (errorsVal / rpsVal) * 100 : 0;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("servicesUp", upVal);
        body.put("rps", Math.round(rpsVal * 100.0) / 100.0);
        body.put("errorRatePercent", Math.round(errorRate * 100.0) / 100.0);
        body.put("dbConnectionsActive", Math.round(dbVal * 100.0) / 100.0);
        return ResponseEntity.ok(body);
    }

    private double safeValue(Map<String, Object> result) {
        if (result == null) {
            return 0.0;
        }
        Double v = metricsService.value(result);
        return v != null && !v.isNaN() ? v : 0.0;
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debug() {
        // LinkedHashMap допускает null-значения, в отличие от Map.of,
        // который бросает NullPointerException при любом null (это и давало 500).
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("up", metricsService.query("sum(up{job!=\"api-gateway\"})"));
        body.put("hikari", metricsService.query("sum(hikaricp_connections_active{application!=\"api-gateway\"})"));
        body.put("rps", metricsService.query("sum(rate(http_server_requests_seconds_count[5m]))"));
        return ResponseEntity.ok(body);
    }
}