package co.edu.uptc.taller.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad que define las reglas de comision para cada tipo de transaccion.
 * Permite configurar dinamicamente las comisiones sin modificar codigo.
 * Indexado por tipo de transaccion para optimizar calculos de comision - Trade-off: rendimiento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commission_rules")
public class ComissionRule {

    @Id
    private String id;

    // Indexado para busqueda rapida por tipo de transaccion
    @Indexed(unique = true)
    private TransactionType transactionType;

    // Porcentaje de comision (ej: 0.01 = 1%)
    private BigDecimal percentage;

    // Comision minima fija
    private BigDecimal minAmount;

    // Comision maxima fija
    private BigDecimal maxAmount;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}
