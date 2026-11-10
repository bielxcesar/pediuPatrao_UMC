package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.service.DashboardService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/resumo")
    public ResponseEntity<DashboardService.Resumo> resumo(
            @RequestParam(required = false) String inicio, @RequestParam(required = false) String fim) {
        LocalDate ate = fim == null ? LocalDate.now(FUSO) : LocalDate.parse(fim);
        LocalDate desde = inicio == null ? ate.minusDays(29) : LocalDate.parse(inicio);
        if (desde.getYear() < 1 || ate.getYear() > 9999 || ate.isBefore(desde)
                || ate.isAfter(desde.plusDays(365))) {
            throw new PeriodoInvalido();
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(dashboard.consultar(desde, ate));
    }

    @ExceptionHandler({DateTimeParseException.class, PeriodoInvalido.class})
    ProblemDetail periodoInvalido() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Informe datas no formato AAAA-MM-DD, em ordem, com no máximo 366 dias.");
    }

    private static class PeriodoInvalido extends RuntimeException {}
}
