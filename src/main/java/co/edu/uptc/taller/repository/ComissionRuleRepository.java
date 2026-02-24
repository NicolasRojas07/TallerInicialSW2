package co.edu.uptc.taller.repository;

import co.edu.uptc.taller.model.ComissionRule;
import co.edu.uptc.taller.model.TransactionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones de persistencia de reglas de comision.
 * Busqueda optimizada por tipo de transaccion con indice unico.
 */
@Repository
public interface ComissionRuleRepository extends MongoRepository<ComissionRule, String> {

    // Buscar regla de comision por tipo de transaccion - optimizado con indice unico
    Optional<ComissionRule> findByTransactionTypeAndActiveTrue(TransactionType transactionType);
}
