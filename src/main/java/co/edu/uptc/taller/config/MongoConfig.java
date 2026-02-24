package co.edu.uptc.taller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * Configuracion de MongoDB para la aplicacion.
 * 
 * Optimizaciones aplicadas:
 * - Indices en campos de consulta frecuente (userId, accountId, createdAt)
 * - Transacciones para operaciones atomicas (actualizar saldo + crear transaccion)
 * - Document design optimizado para reducir joins
 * 
 * Trade-offs:
 * - Indices mejoran lectura pero enlentecen escritura - Trade-off: rendimiento
 * - Transacciones garantizan consistencia pero reducen throughput - Trade-off: seguridad vs rendimiento
 * 
 * Los indices se definen en las entidades con @Indexed.
 */
@Configuration
public class MongoConfig {

    /**
     * Habilita transacciones en MongoDB.
     * Necesario para operaciones atomicas que modifican multiples documentos.
     * 
     * Trade-off: Garantiza consistencia (ACID) pero reduce throughput.
     * Critico para operaciones como procesar transaccion (actualizar saldo + crear registro).
     * 
     * @param dbFactory Factory de base de datos MongoDB
     * @return Gestor de transacciones
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
