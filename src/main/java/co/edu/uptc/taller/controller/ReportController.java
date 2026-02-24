package co.edu.uptc.taller.controller;

import co.edu.uptc.taller.dto.TransactionSummaryResponse;
import co.edu.uptc.taller.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Controlador REST para generacion de reportes y resumenes.
 * Expone endpoints para consultar estadisticas de transacciones.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Genera resumen completo de transacciones de un usuario.
     * 
     * @param userId ID del usuario
     * @return Resumen con estadisticas y transacciones
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<TransactionSummaryResponse> getUserSummary(@PathVariable String userId) {
        TransactionSummaryResponse summary = reportService.getUserTransactionSummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Genera resumen de transacciones de un usuario por rango de fechas.
     * 
     * @param userId ID del usuario
     * @param startDate Fecha inicio (formato ISO-8601)
     * @param endDate Fecha fin (formato ISO-8601)
     * @return Resumen del periodo especificado
     */
    @GetMapping("/user/{userId}/summary/range")
    public ResponseEntity<TransactionSummaryResponse> getUserSummaryByRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        
        TransactionSummaryResponse summary = reportService.getUserTransactionSummaryByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Genera resumen completo de transacciones de una cuenta.
     * 
     * @param accountId ID de la cuenta
     * @return Resumen con estadisticas y transacciones
     */
    @GetMapping("/account/{accountId}/summary")
    public ResponseEntity<TransactionSummaryResponse> getAccountSummary(@PathVariable String accountId) {
        TransactionSummaryResponse summary = reportService.getAccountTransactionSummary(accountId);
        return ResponseEntity.ok(summary);
    }
}
