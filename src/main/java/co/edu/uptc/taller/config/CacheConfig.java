package co.edu.uptc.taller.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuracion de cache para optimizar rendimiento.
 * 
 * Trade-offs implementados:
 * - Cache en memoria: Rapido pero limitado por RAM - Trade-off: rendimiento vs recursos
 * - TTL implicito: Los datos cacheados pueden quedar desactualizados - Trade-off: rendimiento vs consistencia
 * - Cache de comisiones: Reduce consultas a BD en calculos frecuentes - Trade-off: rendimiento
 * - Cache de reportes: Mejora tiempo de respuesta en consultas agregadas - Trade-off: rendimiento vs consistencia
 * 
 * En produccion con 5000 usuarios concurrentes, considerar:
 * - Redis o Hazelcast para cache distribuido
 * - Configurar TTL adecuados segun frecuencia de cambios
 * - Monitorear hit rate del cache
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura el gestor de cache de la aplicacion.
     * 
     * Caches definidos:
     * - commissions: Cache para calculo de comisiones (reduce consultas a reglas)
     * - userSummaries: Cache para resumenes de usuario (reduce agregaciones)
     * - accountSummaries: Cache para resumenes de cuenta (reduce agregaciones)
     * 
     * Implementacion simple en memoria para desarrollo.
     * En produccion: usar Redis para cache distribuido entre instancias.
     * 
     * @return Gestor de cache configurado
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        cacheManager.setCaches(Arrays.asList(
            // Cache para reglas de comision por tipo de transaccion
            // Alto hit rate esperado: pocas reglas, muchas consultas
            new ConcurrentMapCache("commissions"),
            
            // Cache para resumenes de usuario
            // Hit rate medio: usuarios activos consultan frecuentemente
            new ConcurrentMapCache("userSummaries"),
            
            // Cache para resumenes de cuenta
            // Hit rate medio: cuentas activas consultadas frecuentemente
            new ConcurrentMapCache("accountSummaries")
        ));
        
        return cacheManager;
    }
}
