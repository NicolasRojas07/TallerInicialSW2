package co.edu.uptc.taller.dto;

import co.edu.uptc.taller.model.TransactionType;
import javax.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para la creacion de una nueva transaccion.
 * Incluye validaciones para garantizar integridad de datos - Atributo de Seguridad
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotBlank(message = "UserId is required")
    private String userId;

    @NotBlank(message = "AccountId is required")
    private String accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    private String description;

    // Datos de auditoria opcionales
    private String ipAddress;
    
    private String sessionId;
}
