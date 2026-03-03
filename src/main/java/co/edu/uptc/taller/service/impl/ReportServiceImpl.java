package co.edu.uptc.taller.service.impl;

import co.edu.uptc.taller.dto.TransactionResponse;
import co.edu.uptc.taller.dto.TransactionSummaryResponse;
import co.edu.uptc.taller.model.Transaction;
import co.edu.uptc.taller.repository.TransactionRepository;
import co.edu.uptc.taller.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de reportes.
 * Utiliza cache para optimizar reportes frecuentes - Trade-off: rendimiento vs consistencia
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Genera resumen de transacciones de un usuario.
     * Cache con TTL para balance entre rendimiento y frescura de datos.
     * 
     * @param userId ID del usuario
     * @return Resumen con todas las transacciones y estadisticas
     */
    @Override
    @Cacheable(value = "userSummaries", key = "#userId")
    public TransactionSummaryResponse getUserTransactionSummary(String userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return buildSummary(userId, null, transactions);
    }

    /**
     * Genera resumen de transacciones de un usuario en un rango de fechas.
     * Util para reportes mensuales, trimestrales, etc.
     * 
     * @param userId ID del usuario
     * @param startDate Fecha inicio
     * @param endDate Fecha fin
     * @return Resumen del periodo especificado
     */
    @Override
    public TransactionSummaryResponse getUserTransactionSummaryByDateRange(String userId, Instant startDate, Instant endDate) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);
        return buildSummary(userId, null, transactions);
    }

    /**
     * Genera resumen de transacciones de una cuenta especifica.
     * Cache para optimizar consultas repetidas.
     * 
     * @param accountId ID de la cuenta
     * @return Resumen con todas las transacciones de la cuenta
     */
    @Override
    @Cacheable(value = "accountSummaries", key = "#accountId")
    public TransactionSummaryResponse getAccountTransactionSummary(String accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        String userId = transactions.isEmpty() ? null : transactions.getFirst().getUserId();
        return buildSummary(userId, accountId, transactions);
    }

    /**
     * Construye el resumen agregando estadisticas de las transacciones.
     * Calcula totales de montos y comisiones.
     * 
     * @param userId ID del usuario
     * @param accountId ID de la cuenta
     * @param transactions Lista de transacciones a resumir
     * @return Resumen construido con estadisticas
     */
    private TransactionSummaryResponse buildSummary(String userId, String accountId, List<Transaction> transactions) {
        // Calcula monto total de transacciones
        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcula total de comisiones cobradas
        BigDecimal totalCommissions = transactions.stream()
                .map(Transaction::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convierte transacciones a DTOs de respuesta
        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return TransactionSummaryResponse.builder()
                .userId(userId)
                .accountId(accountId)
                .totalTransactions(transactions.size())
                .totalAmount(totalAmount)
                .totalCommissions(totalCommissions)
                .transactions(transactionResponses)
                .build();
    }

    /**
     * Convierte Transaction a TransactionResponse.
     * 
     * @param transaction Entidad a convertir
     * @return DTO de respuesta
     */
    private TransactionResponse convertToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .accountId(transaction.getAccountId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .commission(transaction.getCommission())
                .totalAmount(transaction.getTotalAmount())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .description(transaction.getDescription())
                .build();
    }
}
