package co.edu.uptc.taller.repository;

import co.edu.uptc.taller.model.Transaction;
import co.edu.uptc.taller.model.TransactionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio para operaciones de persistencia de transacciones.
 * Metodos indexados para optimizar consultas frecuentes - Trade-off: rendimiento
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    // Buscar transacciones por usuario - optimizado con indice
    List<Transaction> findByUserId(String userId);

    // Buscar transacciones por cuenta - optimizado con indice
    List<Transaction> findByAccountId(String accountId);

    // Buscar transacciones por estado
    List<Transaction> findByStatus(TransactionStatus status);

    // Buscar transacciones por usuario y rango de fechas - para reportes
    List<Transaction> findByUserIdAndCreatedAtBetween(String userId, Instant startDate, Instant endDate);

    // Buscar transacciones por cuenta y rango de fechas - para reportes
    List<Transaction> findByAccountIdAndCreatedAtBetween(String accountId, Instant startDate, Instant endDate);
}
