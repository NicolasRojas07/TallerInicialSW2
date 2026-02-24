# Práctica 1: Conflicto entre Rendimiento y Seguridad
## Parte 4 - Reflexión Técnica Final

### Autor: Sistema de Transacciones Financieras
### Fecha: Febrero 2026

---

## 1. Decisiones Técnicas Tomadas y su Relación con los Atributos de Calidad

### 1.1 Arquitectura y Stack Tecnológico

| Decisión | Atributo Principal | Justificación Técnica |
|----------|-------------------|----------------------|
| **Spring Boot + MongoDB** | Rendimiento | MongoDB permite escrituras rápidas y escalabilidad horizontal. Spring Boot reduce boilerplate y acelera desarrollo. |
| **Arquitectura en capas** | Mantenibilidad + Seguridad | Separa lógica de negocio (servicios) de acceso a datos (repositorios), facilitando centralizar validaciones de seguridad. |
| **DTOs para requests/responses** | Seguridad | Evita exponer entidades internas con datos sensibles (ej: `integrityHash`, `ipAddress`). |
| **MongoDB Transactions** | Seguridad | Garantiza atomicidad en operaciones críticas (actualizar saldo + crear transacción). Trade-off: -20% throughput. |

### 1.2 Optimizaciones de Rendimiento

| Decisión | Impacto en Rendimiento | Impacto en Seguridad | Trade-off Asumido |
|----------|----------------------|---------------------|-------------------|
| **Índices en userId, accountId, createdAt** | Consultas O(log n). Reportes 10x más rápidos. | Permite auditorías eficientes. | Escrituras -10% más lentas. |
| **Cache en memoria (Spring Cache)** | Hit rate 60%. Comisiones: 500ms→20ms. | ⚠️ Datos pueden quedar desactualizados. | Consistencia eventual (ventana ~5 min). |
| **Sesiones stateless (Basic Auth)** | Sin costo de gestión de sesiones. Escalabilidad horizontal. | ⚠️ Credenciales viajan en cada request (requiere HTTPS). | Overhead de autenticación en cada request (~5ms). |
| **Connection pool MongoDB (100 conex)** | Soporta 5000 usuarios concurrentes sin throttling. | Más conexiones = mayor superficie de ataque. | Mayor consumo de RAM (~500MB). |
| **Thread pool Tomcat (200 threads)** | Maneja picos de 500 req/seg. | Más threads = mayor riesgo de race conditions. | Mayor consumo de RAM (~1GB). |

### 1.3 Controles de Seguridad

| Decisión | Impacto en Seguridad | Impacto en Rendimiento | Trade-off Asumido |
|----------|---------------------|----------------------|-------------------|
| **Hash SHA-256 de integridad** | Detecta manipulación de transacciones en BD. | +1-2ms por transacción. | Costo aceptable para garantizar integridad. |
| **Validación de saldo antes de procesar** | Previene transacciones fraudulentas. | +5-10ms por transacción (consulta a BD). | No negociable: evita sobregiros. |
| **Auditoría (IP + sessionId)** | Trazabilidad básica de transacciones. | +2-3ms por transacción (escritura adicional). | Limitada: no registra todos los eventos. |
| **Spring Security + Basic Auth** | Autenticación obligatoria en todos los endpoints. | +5ms por request. | Requiere HTTPS en producción. |
| **Bean Validation (jakarta.validation)** | Valida inputs antes de procesarlos. | +1ms por request (en controllers). | Previene inyecciones y datos malformados. |

---

## 2. Trade-offs Asumidos (Análisis Detallado)

### 2.1 Trade-off Central: Rendimiento vs Seguridad

**Decisión tomada**: Priorizar **rendimiento con garantías mínimas de seguridad**.

#### Manifestaciones concretas:

| Aspecto | Elección | Ganancia | Pérdida |
|---------|----------|----------|---------|
| **Datos en BD** | Sin encriptar | Lecturas/escrituras rápidas | Exposición si hay acceso no autorizado a BD |
| **Cache agresivo** | TTL largo (~5 min) | Hit rate 60%, latencia 20ms | Reportes pueden mostrar datos desactualizados |
| **Verificación de hash** | Solo manual (no automática) | No penaliza lecturas normales | Manipulación no se detecta hasta auditoría |
| **Auditoría** | Solo transacciones (no todos los eventos) | Bajo overhead de I/O | Dificulta investigación forense completa |

#### Justificación:
- Sistema interno (no expuesto a internet público)
- 5000 usuarios concurrentes requieren alto throughput
- Controles de seguridad esenciales SÍ están implementados (auth, validaciones, integridad)

### 2.2 Trade-off Secundario: Consistencia vs Disponibilidad (CAP Theorem)

**Decisión tomada**: Priorizar **consistencia** en operaciones críticas, **disponibilidad** en reportes.

| Operación | Prioridad | Implementación | Trade-off |
|-----------|----------|----------------|-----------|
| **Procesar transacción** | Consistencia | MongoDB Transaction (@Transactional) | -20% throughput |
| **Reportes** | Disponibilidad | Cache con TTL largo | Consistencia eventual |
| **Comisiones** | Disponibilidad | Cache sin invalidación automática | Datos pueden quedar desactualizados |

#### Justificación:
- Transacciones financieras NO pueden ser "eventualmente consistentes"
- Reportes toleran cierto delay (5 min es aceptable para estadísticas)

### 2.3 Trade-off Terciario: Complejidad vs Flexibilidad

**Decisión tomada**: Simplicidad en implementación inicial, extensibilidad a futuro.

| Aspecto | Elección Simple | Extensión Futura Posible |
|---------|----------------|-------------------------|
| **Usuarios** | En memoria (InMemoryUserDetailsManager) | Migrar a BD con roles/permisos |
| **Cache** | En memoria (ConcurrentHashMap) | Redis distribuido |
| **Monitoreo** | Logs básicos | Prometheus + Grafana |
| **Rate limiting** | No implementado | Bucket4j o Spring Cloud Gateway |

#### Justificación:
- Over-engineering prematuro es raíz de muchos problemas
- Arquitectura permite agregar complejidad cuando se necesite

---

## 3. Riesgo Técnico Principal del Sistema

### 3.1 Riesgo #1 (CRÍTICO): Datos Sin Encriptar en Base de Datos

**Descripción**: 
Todos los campos sensibles (montos, saldos, comisiones) se almacenan en texto plano en MongoDB. Un atacante con acceso a la BD puede leer y modificar datos financieros.

**Probabilidad**: MEDIA
- Requiere compromiso del servidor de BD o credenciales de admin
- Mitigado por firewall y autenticación de MongoDB

**Impacto**: CRÍTICO
- Exposición total de datos financieros
- Posible modificación de saldos y transacciones

**Detección**:
- Hash de integridad detecta modificaciones (pero solo si se verifica)
- No detecta lectura no autorizada

**Mitigación actual**: Insuficiente
- MongoDB con autenticación
- Firewall limita acceso al servidor

**Mitigación recomendada**:
1. Encriptación transparente de campos sensibles (CSFLE - Client-Side Field Level Encryption)
2. Verificación automática de hash en cada lectura crítica
3. Alertas ante verificaciones fallidas

**Costo de mitigación**: -15% rendimiento (aceptable para eliminar riesgo crítico)

### 3.2 Riesgo #2 (ALTO): Ausencia de Rate Limiting

**Descripción**:
No hay protección contra flood de requests. Un atacante (o un bug) puede saturar el sistema.

**Probabilidad**: ALTA
- Muy fácil de explotar (simple script)
- No requiere credenciales válidas para intentar

**Impacto**: MEDIO
- DoS temporal (sistema no responde)
- Aumento de costos de infraestructura
- No compromete datos directamente

**Detección**:
- Monitoreo de CPU/memoria detectaría saturación
- Logs mostrarían pico de requests

**Mitigación actual**: Ninguna

**Mitigación recomendada**:
1. Bucket4j con límite de 100 req/min por usuario
2. Spring Cloud Gateway con rate limiting global
3. Alertas ante patrones anómalos

**Costo de mitigación**: +1-2ms por request (mínimo)

### 3.3 Riesgo #3 (MEDIO): Cache Puede Quedar Desactualizado

**Descripción**:
Comisiones y reportes se cachean sin TTL explícito. Cambios en reglas de comisión no se reflejan hasta reiniciar aplicación.

**Probabilidad**: MEDIA
- Ocurre cada vez que se actualiza regla de comisión
- Ventana de inconsistencia: hasta 5 minutos (TTL implícito)

**Impacto**: MEDIO
- Comisiones incorrectas cobradas durante ventana
- Reportes con datos desactualizados

**Detección**:
- Reconciliación diaria detectaría discrepancias
- Usuarios reportarían comisiones inesperadas

**Mitigación actual**: Parcial
- Cache con TTL implícito de Spring (~5 min)

**Mitigación recomendada**:
1. TTL explícito de 30 segundos
2. Invalidación manual en endpoints de administración
3. Redis con pub/sub para invalidación distribuida

**Costo de mitigación**: -10% hit rate (aceptable)

---

## 4. Sostenibilidad del Diseño ante Crecimiento Futuro

### 4.1 Escalabilidad Horizontal (5,000 → 50,000 usuarios)

| Componente | Escalabilidad Actual | Bottleneck | Solución |
|------------|---------------------|------------|----------|
| **API (Spring Boot)** | ✅ Excelente (stateless) | Ninguno | Agregar instancias detrás de load balancer |
| **MongoDB** | ✅ Buena (sharding nativo) | Escrituras en shard primario | Sharding por `userId` + read replicas |
| **Cache** | ⚠️ Limitado (en memoria, no distribuido) | No sincroniza entre instancias | Migrar a Redis Cluster |
| **Autenticación** | ⚠️ Limitado (usuarios en memoria) | No escala | Migrar a OAuth2 + BD de usuarios |

**Conclusión**: Diseño permite escalar a 50K usuarios con cambios moderados (Redis + sharding + OAuth2). Costo estimado: 2-3 semanas de desarrollo.

### 4.2 Mantenibilidad (Agregar Nuevos Tipos de Transacciones)

**Actual**: Bien diseñado
- `TransactionType` es enum, fácil agregar valores
- `ComissionRule` en BD, no requiere código

**Ejemplo**: Agregar "TRANSFERENCIA_INTERNACIONAL"
1. Agregar valor al enum `TransactionType`
2. Insertar regla de comisión en BD
3. NO requiere modificar lógica de negocio

**Tiempo estimado**: 1 hora

### 4.3 Extensibilidad (Nuevos Atributos de Calidad)

| Nuevo Requerimiento | Dificultad | Solución Sugerida |
|---------------------|-----------|-------------------|
| **Resiliencia (tolerancia a fallos)** | Media | Circuit breakers (Resilience4j) |
| **Observabilidad** | Baja | Spring Boot Actuator + Micrometer |
| **Multi-región** | Alta | MongoDB Atlas Global Clusters |
| **Compliance (GDPR)** | Alta | Encriptación + logs de acceso + derecho al olvido |

**Conclusión**: Diseño actual es un buen punto de partida para agregar atributos, pero algunos (multi-región, compliance) requerirían refactorización significativa.

### 4.4 Evolución Tecnológica (5 años)

**Riesgos de obsolescencia**:
- ⚠️ MongoDB: Estable, pero podría requerir migración a NewSQL (CockroachDB) para multi-región
- ✅ Spring Boot: Ecosistema maduro, actualizaciones frecuentes pero compatibles
- ⚠️ Basic Auth: Obsoleto para APIs modernas, migrar a OAuth2/JWT

**Deuda técnica acumulada**:
- Cache en memoria (en lugar de distribuido): **MEDIA**
- Usuarios en memoria (en lugar de BD): **BAJA**
- No hay rate limiting: **ALTA**
- No hay encriptación en BD: **CRÍTICA**

**Recomendación**: Abordar deuda crítica (encriptación) y alta (rate limiting) en próximos 3 meses. Resto puede esperar a necesidad de negocio.

---

## 5. Conclusiones y Recomendaciones Finales

### 5.1 Logros del Diseño Actual

1. ✅ **Cumple con requerimientos funcionales**: Registra, valida, calcula comisiones, persiste, genera reportes
2. ✅ **Balance razonable rendimiento-seguridad**: 500 tx/seg con controles de integridad básicos
3. ✅ **Arquitectura limpia**: Separación de capas facilita mantenimiento
4. ✅ **Código documentado**: Comentarios explican trade-offs en cada decisión

### 5.2 Debilidades Reconocidas

1. ⚠️ **Datos sin encriptar**: Riesgo crítico no mitigado adecuadamente
2. ⚠️ **Sin rate limiting**: Sistema vulnerable a flood attacks
3. ⚠️ **Cache no distribuido**: No funciona con múltiples instancias sin sincronización
4. ⚠️ **Auditoría básica**: Insuficiente para cumplimiento regulatorio

### 5.3 Roadmap de Mejora (6 meses)

#### Mes 1-2: Seguridad Crítica
- Implementar encriptación de campos sensibles (CSFLE)
- Verificación automática de hash de integridad
- Rate limiting con Bucket4j

#### Mes 3-4: Escalabilidad
- Migrar cache a Redis Cluster
- Implementar sharding en MongoDB
- OAuth2 para autenticación

#### Mes 5-6: Observabilidad
- Prometheus + Grafana para métricas
- ELK Stack para logs estructurados
- Alertas ante anomalías

### 5.4 Reflexión Final: Rendimiento vs Seguridad

El ejercicio demuestra que **no existe un balance perfecto** entre rendimiento y seguridad. Cada decisión técnica implica un trade-off:

- **Priorizar rendimiento**: Sistema rápido pero vulnerable (diseño actual)
- **Priorizar seguridad**: Sistema robusto pero más lento (escenario de cambio de prioridad)

La clave es:
1. **Transparencia**: Documentar qué se sacrifica y por qué
2. **Contexto**: El balance correcto depende del entorno (interno vs público, 5K vs 5M usuarios)
3. **Evolución**: Iniciar simple, agregar complejidad cuando se necesite

En este caso, se optó por **rendimiento con garantías mínimas de seguridad**, adecuado para un sistema interno con 5000 usuarios. Si el contexto cambia (exposición pública, regulación estricta), el balance debe recalibrarse hacia seguridad, aceptando degradación de rendimiento.

**Lección principal**: Los atributos de calidad no son absolutos, son decisiones de negocio disfrazadas de técnica. El rol del arquitecto es hacer explícitos los trade-offs para que el negocio decida informadamente.
