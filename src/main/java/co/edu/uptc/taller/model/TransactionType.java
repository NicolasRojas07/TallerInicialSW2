package co.edu.uptc.taller.model;

/**
 * Enum que define los tipos de transacciones soportados por el sistema.
 * Cada tipo tiene asociada una tasa de comision diferente.
 */
public enum TransactionType {
    TRANSFER,      // Transferencia entre cuentas
    DEPOSIT,       // Deposito a cuenta
    WITHDRAWAL,    // Retiro de cuenta
    PAYMENT        // Pago de servicio
}
