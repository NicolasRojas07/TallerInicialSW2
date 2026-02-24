package co.edu.uptc.taller.model;

/**
 * Enum que representa el estado de una transaccion en su ciclo de vida.
 * Permite rastrear si la transaccion fue exitosa, fallo o esta pendiente.
 */
public enum TransactionStatus {
    PENDING,    // Transaccion pendiente de procesar
    COMPLETED,  // Transaccion completada exitosamente
    FAILED,     // Transaccion fallida
    CANCELLED   // Transaccion cancelada
}
