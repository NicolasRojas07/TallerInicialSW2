package co.edu.uptc.taller.service;

import co.edu.uptc.taller.dto.TransactionSummaryResponse;

import java.time.Instant;

/**
 * Servicio para generar reportes y resumenes de transacciones.
 * Optimizado para consultas de agregacion.
 */
public interface ReportService {

    /**
     * Genera un resumen completo de transacciones para un usuario.
     * Incluye estadisticas agregadas y lista de transacciones.
     * 
     * @param userId ID del usuario
     * @return Resumen con estadisticas y transacciones
     */
    TransactionSummaryResponse getUserTransactionSummary(String userId);

    /**
     * Genera un resumen de transacciones para un usuario en un rango de fechas.
     * 
     * @param userId ID del usuario
     * @param startDate Fecha inicio del periodo
     * @param endDate Fecha fin del periodo
     * @return Resumen con estadisticas y transacciones del periodo
     */
    TransactionSummaryResponse getUserTransactionSummaryByDateRange(String userId, Instant startDate, Instant endDate);

    /**
     * Genera un resumen completo de transacciones para una cuenta.
     * 
     * @param accountId ID de la cuenta
     * @return Resumen con estadisticas y transacciones
     */
    TransactionSummaryResponse getAccountTransactionSummary(String accountId);
}
