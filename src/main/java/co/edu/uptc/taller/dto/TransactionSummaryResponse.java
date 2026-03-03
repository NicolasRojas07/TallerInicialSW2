package co.edu.uptc.taller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para resumen de transacciones de un usuario o cuenta.
 * Proporciona estadisticas agregadas para reportes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryResponse {

    private String userId;
    private String accountId;
    private long totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal totalCommissions;
    private List<TransactionResponse> transactions;
}
