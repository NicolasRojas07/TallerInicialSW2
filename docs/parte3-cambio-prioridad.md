# Práctica 1: Conflicto entre Rendimiento y Seguridad
## Parte 3 - Cambio de Prioridad Estratégica

### Autor: Sistema de Transacciones Financieras
### Fecha: Febrero 2026

---

## Nuevo Escenario

**Situación**: Se detectaron intentos de manipular transacciones en el sistema. La dirección decide que **la seguridad pasa a ser la prioridad estratégica dominante**. Se acepta una degradación moderada del rendimiento si es necesario.

---

## 1. Análisis de Vulnerabilidades del Diseño Actual

### 1.1 Vulnerabilidad CRÍTICA: Datos sin Encriptar en Base de Datos

**Descripción**: Los documentos en MongoDB se almacenan en texto plano. Un atacante con acceso a la BD puede:
- Leer montos de transacciones
- Ver saldos de cuentas
- Modificar directamente registros (si el hash de integridad no se verifica)

**Evidencia en código**:
```java
// Transaction.java - No hay encriptación de campos sensibles
private BigDecimal amount;
private BigDecimal commission;
private String userId;
```

**Severidad**: CRÍTICA
**Probabilidad**: MEDIA (requiere acceso a BD)
**Impacto**: ALTO (exposición total de datos financieros)

### 1.2 Vulnerabilidad ALTA: No se Verifica Hash de Integridad Automáticamente

**Descripción**: Aunque se calcula un hash de integridad (`integrityHash`) para cada transacción, el sistema NO verifica automáticamente este hash en operaciones posteriores.

**Evidencia en código**:
```java
// TransactionServiceImpl.java
// Se calcula hash al crear transacción
transaction.setIntegrityHash(calculateIntegrityHash(transaction));

// Pero hay método verifyIntegrity() que NO se llama automáticamente
public boolean verifyIntegrity(Transaction transaction) {
    // Este método existe pero no se usa en flujos normales
}
```

**Severidad**: ALTA
**Probabilidad**: ALTA (si hay acceso a BD)
**Impacto**: ALTO (transacciones manipuladas no se detectan)

### 1.3 Vulnerabilidad ALTA: Cache sin Validación de Integridad

**Descripción**: Las comisiones y reportes se cachean sin verificar que los datos subyacentes no han sido manipulados.

**Evidencia en código**:
```java
// ComissionServiceImpl.java
@Cacheable(value = "commissions", key = "#transactionType")
public BigDecimal calculateCommission(TransactionType transactionType, BigDecimal amount) {
    // Cache puede retornar valores viejos o manipulados
}
```

**Severidad**: ALTA
**Probabilidad**: MEDIA
**Impacto**: MEDIO (comisiones incorrectas hasta invalidar cache)

### 1.4 Vulnerabilidad MEDIA: No hay Rate Limiting

**Descripción**: No existe protección contra flood de requests. Un atacante puede:
- Probar múltiples combinaciones de cuentas
- Causar DoS por agotamiento de recursos
- Ejecutar ataques de fuerza bruta

**Evidencia**: No hay ninguna implementación de rate limiting en controladores o configuración de Spring Security.

**Severidad**: MEDIA
**Probabilidad**: ALTA
**Impacto**: MEDIO (DoS temporal, no compromete datos)

### 1.5 Vulnerabilidad MEDIA: Basic Auth sin HTTPS Obligatorio

**Descripción**: Las credenciales viajan en Base64 (no encriptadas) en cada request. Sin HTTPS, son legibles en tránsito.

**Evidencia en código**:
```java
// SecurityConfig.java
.httpBasic(Customizer.withDefaults())
// No hay configuración que FUERCE HTTPS
```

**Severidad**: MEDIA en producción, BAJA en red interna
**Probabilidad**: ALTA en producción sin HTTPS
**Impacto**: CRÍTICO (robo de credenciales)

### 1.6 Vulnerabilidad BAJA: Auditoría Incompleta

**Descripción**: Se registran IPs y sessionIds, pero no hay logs estructurados de intentos fallidos, cambios de saldo, o accesos a cuentas ajenas.

**Severidad**: BAJA
**Probabilidad**: ALTA
**Impacto**: BAJO (dificulta investigación post-incidente)

---

## 2. Cambios Propuestos (Seguridad como Prioridad Dominante)

### 2.1 CRÍTICO: Encriptar Campos Sensibles en Base de Datos

**Cambio**: Implementar encriptación transparente de campos sensibles usando Spring Data MongoDB con cifrado AES-256.

**Implementación**:
```java
@Configuration
public class MongoEncryptionConfig {
    
    @Bean
    public MongoClientSettingsBuilderCustomizer encryptionCustomizer() {
        // Configurar Client-Side Field Level Encryption (CSFLE)
        // Encriptar: amount, commission, balance
    }
}
```

**Impacto en Rendimiento**:
- ⚠️ Latencia de escritura: +10-15ms por transacción
- ⚠️ Latencia de lectura: +5-10ms por transacción
- ⚠️ Throughput: -15% (~425 tx/seg vs 500 tx/seg)

**Impacto en Seguridad**:
- ✅ Datos ilegibles sin clave de encriptación
- ✅ Cumple estándares PCI-DSS para datos financieros

**Justificación**: El costo de rendimiento es aceptable dado que protege el activo más crítico (datos financieros).

### 2.2 CRÍTICO: Verificación Automática de Integridad

**Cambio**: Verificar hash de integridad en CADA lectura de transacción crítica.

**Implementación**:
```java
// TransactionServiceImpl.java
@Override
public Transaction getTransactionById(String id) {
    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    
    // NUEVO: Verificar integridad
    if (!verifyIntegrity(transaction)) {
        // Log crítico + alerta
        logger.error("INTEGRITY VIOLATION: Transaction {} has been tampered", id);
        throw new SecurityException("Transaction integrity compromised");
    }
    
    return transaction;
}
```

**Impacto en Rendimiento**:
- ⚠️ Latencia de lectura: +2-3ms por transacción (recalcular hash)
- ⚠️ No afecta escrituras

**Impacto en Seguridad**:
- ✅ Detecta manipulación en tiempo real
- ✅ Previene uso de datos adulterados

**Justificación**: Costo mínimo para detectar fraude crítico.

### 2.3 ALTA: Invalidar Cache Después de Cada Escritura

**Cambio**: Usar `@CacheEvict` para limpiar cache inmediatamente después de cambios en BD.

**Implementación**:
```java
// ComissionServiceImpl.java
@CacheEvict(value = "commissions", allEntries = true)
public void updateCommissionRule(ComissionRule rule) {
    comissionRuleRepository.save(rule);
}

// TransactionServiceImpl.java
@CacheEvict(value = {"userSummaries", "accountSummaries"}, allEntries = true)
@Override
public TransactionResponse processTransaction(CreateTransactionRequest request) {
    // ... procesar transacción
}
```

**Impacto en Rendimiento**:
- ⚠️ Hit rate de cache: 60% → 30% (más misses)
- ⚠️ Latencia promedio de reportes: 20ms → 150ms
- ⚠️ Carga en BD: +40% de queries

**Impacto en Seguridad**:
- ✅ Datos siempre frescos
- ✅ No hay ventana de inconsistencia

**Justificación**: Prioridad es consistencia sobre velocidad de reportes.

### 2.4 ALTA: Implementar Rate Limiting

**Cambio**: Agregar Bucket4j para limitar requests por usuario.

**Implementación**:
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String username = request.getRemoteUser();
        Bucket bucket = cache.computeIfAbsent(username, this::createBucket);
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // Too Many Requests
        }
    }
    
    private Bucket createBucket(String username) {
        // 100 requests por minuto por usuario
        return Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
            .build();
    }
}
```

**Impacto en Rendimiento**:
- ⚠️ Latencia: +1-2ms por request (overhead de filtro)
- ✅ Protege sistema de sobrecarga

**Impacto en Seguridad**:
- ✅ Previene flood attacks
- ✅ Dificulta ataques de fuerza bruta

**Justificación**: Overhead mínimo para protección significativa.

### 2.5 ALTA: Forzar HTTPS en Producción

**Cambio**: Configurar Spring Security para rechazar HTTP.

**Implementación**:
```java
// SecurityConfig.java
@Override
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .requiresChannel(channel -> channel
            .anyRequest().requiresSecure() // FUERZA HTTPS
        )
        .httpBasic(Customizer.withDefaults());
    
    return http.build();
}
```

**Impacto en Rendimiento**:
- ⚠️ Overhead SSL/TLS: +5-10ms por request
- ⚠️ Costo de handshake inicial: ~50ms

**Impacto en Seguridad**:
- ✅ Credenciales encriptadas en tránsito
- ✅ Protección contra MITM

**Justificación**: Esencial para Basic Auth, no negociable.

### 2.6 MEDIA: Logging Estructurado de Eventos de Seguridad

**Cambio**: Implementar logging detallado con ELK o similar.

**Implementación**:
```java
@Aspect
@Component
public class SecurityAuditAspect {
    
    @AfterReturning("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void logSecurityEvent(JoinPoint joinPoint) {
        // Log: usuario, IP, endpoint, timestamp, resultado
        logger.info("SECURITY_EVENT: user={}, ip={}, endpoint={}, status={}",
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    getCurrentIp(),
                    joinPoint.getSignature(),
                    "SUCCESS");
    }
    
    @AfterThrowing("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void logSecurityFailure(JoinPoint joinPoint, Exception ex) {
        logger.error("SECURITY_FAILURE: user={}, ip={}, endpoint={}, error={}",
                     SecurityContextHolder.getContext().getAuthentication().getName(),
                     getCurrentIp(),
                     joinPoint.getSignature(),
                     ex.getMessage());
    }
}
```

**Impacto en Rendimiento**:
- ⚠️ Overhead de logging: +2-3ms por request
- ⚠️ I/O de disco asíncrono

**Impacto en Seguridad**:
- ✅ Trazabilidad completa
- ✅ Facilita investigación forense

**Justificación**: Costo mínimo para cumplimiento regulatorio.

---

## 3. Impacto Concreto en Rendimiento (Resumen)

### Métricas Antes (Prioridad: Rendimiento)
| Métrica | Valor Actual |
|---------|--------------|
| Throughput | 500 tx/seg |
| Latencia P95 | 80ms |
| Latencia P99 | 150ms |
| Tiempo de reportes | 20ms (cache hit) |

### Métricas Después (Prioridad: Seguridad)
| Métrica | Valor Proyectado | Δ |
|---------|------------------|---|
| Throughput | **350 tx/seg** | **-30%** |
| Latencia P95 | **180ms** | **+125%** |
| Latencia P99 | **350ms** | **+133%** |
| Tiempo de reportes | **200ms** | **+900%** |

### Desglose de Impacto por Cambio

| Cambio | Δ Latencia | Δ Throughput |
|--------|-----------|--------------|
| Encriptación BD | +15ms | -15% |
| Verificación hash | +3ms | 0% |
| Invalidar cache | +0ms (indirecto) | -10% |
| Rate limiting | +2ms | -5% |
| HTTPS | +10ms | 0% |
| Logging | +3ms | 0% |
| **TOTAL** | **+33ms** | **-30%** |

### Análisis del Impacto

**¿Es aceptable?**
- ✅ SÍ: 350 tx/seg aún soporta 5000 usuarios (70% capacidad original)
- ✅ SÍ: Latencia de 180ms es perceptible pero tolerable
- ⚠️ MARGINAL: Reportes de 200ms pueden frustrar usuarios habituados a 20ms

**Mitigaciones posibles**:
1. Escalar horizontalmente (+2 instancias = recuperar throughput)
2. Cache distribuido (Redis) para mantener hit rate alto
3. Índices adicionales para compensar invalidación de cache

---

## 4. Recomendación Final

**Implementar TODOS los cambios propuestos** excepto invalidación agresiva de cache (2.3). En su lugar:

- **Compromiso**: Cache con TTL de 30 segundos
- **Beneficio**: Mantiene 50% hit rate + datos razonablemente frescos
- **Costo**: Ventana de 30 seg para detectar manipulación en reportes

Con este ajuste:
- Throughput: **400 tx/seg** (-20% vs original)
- Latencia P95: **150ms** (+87% vs original)
- Reportes: **80ms promedio** (+300% vs original, aún aceptable)

**Conclusión**: Se logra un balance donde la seguridad es dominante, pero el rendimiento no colapsa. El sistema sigue siendo usable y ahora es significativamente más resistente a manipulación.
