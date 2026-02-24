# Práctica 1: Conflicto entre Rendimiento y Seguridad
## Parte 1 - Diseño Inicial

### Autor: Sistema de Transacciones Financieras
### Fecha: Febrero 2026

---

## 1. Atributo Priorizado: Rendimiento (con garantías de seguridad)

### Justificación de la Decisión

Se ha decidido **priorizar ligeramente el RENDIMIENTO**, manteniendo garantías mínimas de seguridad, por las siguientes razones técnicas:

1. **Contexto de 5,000 usuarios concurrentes**: El sistema debe procesar transacciones constantes durante el día, lo que implica un alto volumen de operaciones por segundo.

2. **Naturaleza del negocio**: Las transacciones financieras internas no están expuestas a internet público inicialmente, reduciendo el vector de amenazas externas.

3. **Experiencia de usuario crítica**: Los tiempos de respuesta lentos pueden causar reintentos, duplicación de transacciones y frustración del usuario.

4. **Escalabilidad futura**: Un diseño optimizado para rendimiento facilita el crecimiento sin requerir refactorización completa.

**Sin embargo**, la seguridad NO se ignora. Se implementan controles esenciales:
- Autenticación obligatoria en todos los endpoints
- Validación de integridad de transacciones mediante hash SHA-256
- Validaciones de negocio estrictas (saldo suficiente, cuenta activa)
- Auditoría básica (IP, sessionId, timestamps)

---

## 2. Decisiones Estructurales que Impactan Rendimiento y Seguridad

### 2.1 Arquitectura de Capas (Servicios)

**Decisión**: Separación clara entre controladores, servicios y repositorios.

**Impacto en Rendimiento**: 
- ✅ Facilita caching a nivel de servicio
- ✅ Permite optimización independiente de cada capa
- ⚠️ Agrega ligera latencia por llamadas entre capas

**Impacto en Seguridad**:
- ✅ Centraliza validaciones en servicios
- ✅ Evita duplicación de lógica de negocio

### 2.2 Base de Datos MongoDB

**Decisión**: Usar MongoDB en lugar de SQL relacional.

**Impacto en Rendimiento**:
- ✅ Escrituras rápidas (importante para transacciones)
- ✅ Escalabilidad horizontal nativa
- ✅ Schema flexible permite optimizar por caso de uso

**Impacto en Seguridad**:
- ⚠️ Transacciones ACID más complejas que en SQL
- ✅ Implementamos transacciones MongoDB para operaciones críticas

### 2.3 Índices en Campos Clave

**Decisión**: Indexar `userId`, `accountId`, `createdAt` en la colección de transacciones.

**Impacto en Rendimiento**:
- ✅ Consultas por usuario/cuenta son O(log n) en lugar de O(n)
- ✅ Reportes por fecha son eficientes
- ⚠️ Escrituras son ~10-15% más lentas (costo aceptable)

**Impacto en Seguridad**:
- ✅ Permite auditorías rápidas por usuario
- ✅ Facilita detección de patrones anómalos

### 2.4 Cache en Memoria (Spring Cache)

**Decisión**: Cachear cálculos de comisiones y reportes de usuario.

**Impacto en Rendimiento**:
- ✅ Reduce consultas a BD en ~60% para operaciones frecuentes
- ✅ Tiempo de respuesta de reportes: 500ms → 20ms

**Impacto en Seguridad**:
- ⚠️ Datos cacheados pueden quedar desactualizados
- ⚠️ Cache en memoria no es seguro contra dumps de memoria
- ✅ TTL implícito limita ventana de inconsistencia

### 2.5 Hash de Integridad SHA-256

**Decisión**: Calcular hash de datos críticos de cada transacción.

**Impacto en Rendimiento**:
- ⚠️ Costo computacional: ~1-2ms por transacción
- ⚠️ Aumenta tamaño de documentos en BD (~64 bytes/transacción)

**Impacto en Seguridad**:
- ✅ Detecta manipulación de datos en BD
- ✅ Permite auditorías de integridad posteriores

### 2.6 Sesiones Stateless (JWT implícito en Basic Auth)

**Decisión**: No mantener sesiones en servidor, usar Basic Auth con credenciales en cada request.

**Impacto en Rendimiento**:
- ✅ No hay costo de gestión de sesiones
- ✅ Escalabilidad horizontal sin sticky sessions
- ⚠️ Overhead de autenticación en cada request (~5ms)

**Impacto en Seguridad**:
- ⚠️ Credenciales viajan en cada request (REQUIERE HTTPS)
- ✅ No hay riesgo de robo de sesión
- ✅ No hay expiración de sesión a gestionar

### 2.7 Transacciones Atómicas

**Decisión**: Usar `@Transactional` para operaciones críticas (actualizar saldo + crear transacción).

**Impacto en Rendimiento**:
- ⚠️ Reduce throughput en ~20% vs operaciones no transaccionales
- ⚠️ Puede causar contención bajo alta concurrencia

**Impacto en Seguridad**:
- ✅ CRÍTICO: Garantiza que no se puede crear transacción sin descontar saldo
- ✅ Evita inconsistencias en caso de falla

---

## 3. Sacrificios Aceptados desde el Inicio

### 3.1 Sacrificios en Seguridad (para ganar rendimiento)

| Sacrificio | Impacto | Mitigación |
|------------|---------|------------|
| **No encriptación de datos en BD** | Datos legibles si hay acceso a BD | MongoDB con autenticación + firewall |
| **Cache puede quedar desactualizado** | Reportes con datos de hace ~5 min | TTL corto + invalidación manual |
| **Auditoría básica (no completa)** | No se registran todos los eventos | Se registra lo crítico: transacciones |
| **Usuarios en memoria (no BD)** | No escalable para muchos usuarios | Suficiente para 5000 usuarios |

### 3.2 Sacrificios en Rendimiento (para ganar seguridad)

| Sacrificio | Impacto | Justificación |
|------------|---------|---------------|
| **Cálculo de hash en cada transacción** | +1-2ms por transacción | Integridad es no negociable |
| **Validación de saldo antes de procesar** | +5-10ms por transacción | Evita fraude básico |
| **Transacciones atómicas** | -20% throughput | Consistencia es crítica |
| **Índices en BD** | Escrituras -10% más lentas | Lecturas son más frecuentes |

### 3.3 Límites Técnicos Reconocidos

1. **No hay rate limiting**: Sistema vulnerable a flood de requests
2. **No hay detección de fraude en tiempo real**: Se detecta a posteriori
3. **No hay replicación de BD**: Punto único de falla
4. **Cache no distribuido**: No funciona con múltiples instancias sin sticky sessions

---

## 4. Métricas Objetivo (SLA)

Con este diseño, esperamos:

| Métrica | Objetivo | Justificación |
|---------|----------|---------------|
| Throughput | 500 transacciones/segundo | 5000 usuarios * 0.1 tx/seg |
| Latencia P95 | < 100ms | Experiencia fluida |
| Latencia P99 | < 200ms | Casos excepcionales |
| Disponibilidad | 99.5% | ~3.6 horas downtime/mes aceptable |
| Tiempo de respuesta de reportes | < 500ms | Datos casi en tiempo real |

---

## 5. Conclusión del Diseño Inicial

Se ha optado por un diseño que **prioriza rendimiento con garantías mínimas de seguridad**. Las decisiones estructurales reflejan este balance:

- **MongoDB + Índices**: Optimiza lectura/escritura
- **Cache agresivo**: Reduce latencia en operaciones frecuentes
- **Sesiones stateless**: Escalabilidad horizontal
- **Hash de integridad**: Detecta manipulación
- **Transacciones atómicas**: Consistencia crítica

Los principales riesgos son:
1. Datos en BD sin encriptar
2. No hay rate limiting
3. Auditoría básica

Estos riesgos son **aceptables** para un sistema interno con 5000 usuarios concurrentes, donde el rendimiento es crítico para la experiencia de usuario.
