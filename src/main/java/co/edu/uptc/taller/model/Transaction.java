package co.edu.uptc.taller.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad que representa una transaccion financiera en el sistema.
 * Contiene toda la informacion necesaria para procesar, validar y auditar transacciones.
 * Se aplica indexacion en campos clave para mejorar el rendimiento de consultas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    // Indexado para optimizar busquedas por usuario - Trade-off: rendimiento vs espacio
    @Indexed
    private String userId;

    // Indexado para optimizar busquedas por cuenta origen
    @Indexed
    private String accountId;

    @NonNull
    private BigDecimal amount;

    @NonNull
    private TransactionType type;

    @NonNull
    private TransactionStatus status;

    private BigDecimal commission;

    private BigDecimal totalAmount;

    // Indexado para optimizar consultas por rango de fechas en reportes
    @Indexed
    private Instant createdAt;

    private Instant processedAt;

    // Hash SHA-256 de los datos criticos para verificar integridad - Atributo de Seguridad
    private String integrityHash;

    private String description;

    // Informacion para auditoria y trazabilidad - Atributo de Seguridad
    private String ipAddress;

    private String sessionId;
}
