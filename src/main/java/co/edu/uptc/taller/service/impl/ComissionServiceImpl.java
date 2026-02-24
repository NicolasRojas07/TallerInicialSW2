package co.edu.uptc.taller.service.impl;

import co.edu.uptc.taller.model.ComissionRule;
import co.edu.uptc.taller.model.TransactionType;
import co.edu.uptc.taller.repository.ComissionRuleRepository;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Implementacion del servicio de comisiones.
 * Utiliza cache para mejorar rendimiento en calculos frecuentes - Trade-off: rendimiento
 * Las reglas se cargan desde BD para permitir configuracion dinamica - Trade-off: seguridad
 */
@Service
public class ComissionServiceImpl implements co.edu.uptc.taller.service.ComissionService {

    @Autowired
    private ComissionRuleRepository comissionRuleRepository;

    /**
     * Inicializa las reglas de comision por defecto al iniciar la aplicacion.
     * Se ejecuta una sola vez al arrancar el sistema.
     */
    @PostConstruct
    public void initializeDefaultRules() {
        // Solo inicializar si no existen reglas
        if (comissionRuleRepository.count() == 0) {
            // Regla para transferencias: 0.5% con minimo de $500 y maximo de $10000
            createRule(TransactionType.TRANSFER, new BigDecimal("0.005"), 
                      new BigDecimal("500"), new BigDecimal("10000"));
            
            // Regla para depositos: 0.2% con minimo de $200
            createRule(TransactionType.DEPOSIT, new BigDecimal("0.002"), 
                      new BigDecimal("200"), new BigDecimal("5000"));
            
            // Regla para retiros: 0.7% con minimo de $700
            createRule(TransactionType.WITHDRAWAL, new BigDecimal("0.007"), 
                      new BigDecimal("700"), new BigDecimal("15000"));
            
            // Regla para pagos: 1% con minimo de $1000
            createRule(TransactionType.PAYMENT, new BigDecimal("0.01"), 
                      new BigDecimal("1000"), new BigDecimal("20000"));
        }
    }

    /**
     * Crea una regla de comision en la base de datos.
     * Metodo auxiliar para inicializacion.
     */
    private void createRule(TransactionType type, BigDecimal percentage, 
                           BigDecimal minAmount, BigDecimal maxAmount) {
        ComissionRule rule = ComissionRule.builder()
                .transactionType(type)
                .percentage(percentage)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        comissionRuleRepository.save(rule);
    }

    /**
     * Calcula la comision aplicable a una transaccion.
     * Utiliza cache para evitar consultas repetidas a BD - mejora rendimiento.
     * Cache key: tipo de transaccion - invalida cuando cambia configuracion.
     * 
     * @param transactionType Tipo de transaccion
     * @param amount Monto de la transaccion
     * @return Comision calculada aplicando reglas de negocio
     */
    @Override
    @Cacheable(value = "commissions", key = "#transactionType")
    public BigDecimal calculateCommission(TransactionType transactionType, BigDecimal amount) {
        ComissionRule rule = getCommissionRule(transactionType);
        
        // Calcula comision base como porcentaje del monto
        BigDecimal commission = amount.multiply(rule.getPercentage())
                .setScale(2, RoundingMode.HALF_UP);
        
        // Aplica comision minima si el calculo es menor
        if (commission.compareTo(rule.getMinAmount()) < 0) {
            commission = rule.getMinAmount();
        }
        
        // Aplica comision maxima si el calculo es mayor
        if (rule.getMaxAmount() != null && commission.compareTo(rule.getMaxAmount()) > 0) {
            commission = rule.getMaxAmount();
        }
        
        return commission;
    }

    /**
     * Obtiene la regla de comision activa para un tipo de transaccion.
     * Lanza excepcion si no existe regla configurada - garantiza integridad.
     * 
     * @param transactionType Tipo de transaccion
     * @return Regla de comision aplicable
     * @throws RuntimeException si no existe regla configurada
     */
    @Override
    public ComissionRule getCommissionRule(TransactionType transactionType) {
        return comissionRuleRepository.findByTransactionTypeAndActiveTrue(transactionType)
                .orElseThrow(() -> new RuntimeException("No commission rule found for transaction type: " + transactionType));
    }
}
