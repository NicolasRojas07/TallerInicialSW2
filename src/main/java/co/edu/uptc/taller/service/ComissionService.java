package co.edu.uptc.taller.service;

import co.edu.uptc.taller.model.ComissionRule;
import co.edu.uptc.taller.model.TransactionType;

import java.math.BigDecimal;

/**
 * Servicio para calcular comisiones de transacciones.
 * Aplica reglas de negocio para diferentes tipos de transacciones.
 */
public interface ComissionService {

    /**
     * Calcula la comision para una transaccion especifica.
     * Utiliza cache para optimizar el calculo - Trade-off: rendimiento vs consistencia
     * 
     * @param transactionType Tipo de transaccion
     * @param amount Monto de la transaccion
     * @return Monto de la comision calculada
     */
    BigDecimal calculateCommission(TransactionType transactionType, BigDecimal amount);

    /**
     * Inicializa las reglas de comision predeterminadas en el sistema.
     */
    void initializeDefaultRules();

    /**
     * Obtiene la regla de comision para un tipo de transaccion.
     * 
     * @param transactionType Tipo de transaccion
     * @return Regla de comision aplicable
     */
    ComissionRule getCommissionRule(TransactionType transactionType);
}