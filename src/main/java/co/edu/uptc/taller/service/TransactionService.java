package co.edu.uptc.taller.service;

import co.edu.uptc.taller.dto.CreateTransactionRequest;
import co.edu.uptc.taller.dto.TransactionResponse;
import co.edu.uptc.taller.model.Transaction;

import java.util.List;

/**
 * Servicio para gestionar transacciones financieras.
 * Implementa validaciones de saldo, integridad y seguridad.
 */
public interface TransactionService {

    /**
     * Procesa una nueva transaccion.
     * Valida saldo, calcula comision, verifica integridad y persiste.
     * Operacion atomica para garantizar consistencia - Trade-off: seguridad
     * 
     * @param request Datos de la transaccion a procesar
     * @return Respuesta con datos de la transaccion procesada
     */
    TransactionResponse processTransaction(CreateTransactionRequest request);

    /**
     * Obtiene una transaccion por su ID.
     * 
     * @param id ID de la transaccion
     * @return Transaccion encontrada
     */
    Transaction getTransactionById(String id);

    /**
     * Obtiene todas las transacciones de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de transacciones del usuario
     */
    List<Transaction> getTransactionsByUserId(String userId);

    /**
     * Obtiene todas las transacciones de una cuenta.
     * 
     * @param accountId ID de la cuenta
     * @return Lista de transacciones de la cuenta
     */
    List<Transaction> getTransactionsByAccountId(String accountId);
}
