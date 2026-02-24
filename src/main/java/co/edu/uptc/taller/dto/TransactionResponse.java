package co.edu.uptc.taller.dto;

import co.edu.uptc.taller.model.TransactionStatus;
import co.edu.uptc.taller.model.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de respuesta para transacciones.
 * Contiene informacion completa de la transaccion procesada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String id;
    private String userId;
    private String accountId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal commission;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant processedAt;
    private String description;
}
