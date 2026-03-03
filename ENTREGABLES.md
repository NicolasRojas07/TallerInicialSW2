# Entregables - Práctica 1: Conflicto entre Rendimiento y Seguridad

## Resumen del Proyecto

Sistema de transacciones financieras desarrollado en Spring Boot + MongoDB que balancea rendimiento y seguridad para soportar 5,000 usuarios concurrentes.

---

## 1. Código Fuente Funcional ✅

### Componentes Implementados

#### Modelos de Dominio
- `Account`: Cuentas bancarias con saldo y estado
- `Transaction`: Transacciones con hash de integridad
- `ComissionRule`: Reglas configurables de comisión
- `TransactionType`: Tipos de transacción (TRANSFER, DEPOSIT, WITHDRAWAL, PAYMENT)
- `TransactionStatus`: Estados de transacción (PENDING, COMPLETED, FAILED)
- `AccountStatus`: Estados de cuenta (ACTIVE, BLOCKED)

#### Servicios (Lógica de Negocio)
- **AccountService**: Gestión de cuentas bancarias
- **TransactionService**: Procesamiento de transacciones con validaciones
  - Valida saldo suficiente
  - Calcula comisión según tipo
  - Genera hash de integridad SHA-256
  - Persiste atomicamente (saldo + transacción) *
- **ComissionService**: Cálculo de comisiones con cache
  - Inicializa reglas por defecto
  - Cache para optimizar cálculos frecuentes
- **ReportService**: Generación de resúmenes con estadísticas
  - Agregaciones eficientes
  - Cache para reportes frecuentes
- **AuthService**: Autenticación y registro de usuarios
  - Login con validación BCrypt
  - Registro con validación de unicidad
  - Verificación de disponibilidad de username/email

_* Nota: Transacciones MongoDB deshabilitadas para compatibilidad con MongoDB standalone_

#### Controladores REST
- **AccountController**: CRUD de cuentas
- **TransactionController**: Procesamiento y consulta de transacciones
- **ReportController**: Generación de reportes y resúmenes
- **AuthController**: Autenticación y registro de usuarios (endpoints públicos)
- **GlobalExceptionHandler**: Manejo centralizado de errores

#### Configuración
- **SecurityConfig**: Autenticación HTTP Basic, CORS, sesiones stateless, endpoints públicos para autenticación
- **CacheConfig**: Cache en memoria para comisiones y reportes
- **MongoConfig**: Configuración de BD (transacciones deshabilitadas para standalone)
- **DataInitializer**: Datos de prueba en desarrollo

#### Frontend
- Interfaz web básica y funcional (HTML + CSS + JavaScript)
- Gestión de cuentas, transacciones y reportes
- **Módulo de autenticación separado** con registro de usuarios
- Mensajes inline sin ventanas emergentes (mejor UX)
- Diseño responsive

### Características Técnicas

**Rendimiento**:
- Índices en campos clave (userId, accountId, createdAt)
- Cache para cálculos frecuentes (hit rate ~60%)
- Connection pool optimizado (100 conexiones)
- Thread pool configurado (200 threads)
- Sesiones stateless para escalabilidad horizontal

**Seguridad**:
- Autenticación obligatoria en endpoints del sistema bancario
- Endpoints públicos para registro y login (módulo de autenticación)
- Hash SHA-256 de integridad por transacción
- Encriptación de contraseñas con BCrypt
- Validación estricta de saldo y permisos
- Auditoría (IP, sessionId, timestamps)
- Transacciones atómicas * (deshabilitadas para MongoDB standalone)
- Bean Validation en inputs

_* Para producción con alta concurrencia, configurar MongoDB como Replica Set y reactivar `@Transactional`_

---

## 2. Documento de Diseño Inicial ✅

**Ubicación**: `docs/parte1-diseno-inicial.md`

### Contenido

1. **Atributo Priorizado**: Rendimiento (con garantías mínimas de seguridad)
   - Justificación técnica
   - Contexto de 5000 usuarios concurrentes

2. **Decisiones Estructurales**:
   - Arquitectura de capas
   - Base de datos MongoDB
   - Índices en campos clave
   - Cache en memoria
   - Hash de integridad SHA-256
   - Sesiones stateless
   - Transacciones atómicas

3. **Sacrificios Aceptados**:
   - Seguridad sacrificada: No encriptación en BD, cache desactualizado, auditoría básica
   - Rendimiento sacrificado: Cálculo de hash, validaciones, transacciones atómicas, índices
   - Límites reconocidos: Sin rate limiting, sin detección de fraude en tiempo real

4. **Métricas Objetivo**:
   - Throughput: 500 tx/seg
   - Latencia P95: <100ms
   - Latencia P99: <200ms
   - Disponibilidad: 99.5%

---

## 2. Documento Técnico: Patrón Adaptador ✅

**Ubicación**: `docs/parte2-adaptador.md`

### Contenido

1. **Acoplamiento inicial identificado**:
   - `TransactionServiceImpl` inyectaba directamente `AccountRepository` (MongoRepository — tecnología concreta)
   - Dependencia directa hacia Spring Data MongoDB en la capa de negocio
   - Inconsistencia: `ComissionService` sí se inyectaba como interfaz

2. **Cambios encapsulados por el adaptador** (`AccountService`):
   - Estrategia de resolución de cuenta (por ID o por userId)
   - Tecnología de persistencia subyacente
   - Validaciones de unicidad de cuenta por usuario
   - Mensajes de error al consumidor
   - Contratos internos de consulta (Spring Data MongoDB)

3. **Mejora de modificabilidad** (ISO 25010):
   - Clases a modificar si cambia la BD: de ≥3 a 1 (`AccountServiceImpl`)
   - Inversión de dependencias: `TransactionService` depende de la abstracción, no de la implementación
   - Escenario concreto: migrar a Redis solo requiere nueva implementación de la interfaz

4. **Costo de la solución**:
   - Archivos adicionales (interfaz + implementacion)
   - Indirección AOP Spring (~1–3 µs por llamada — despreciable frente a MongoDB ~1–5 ms)
   - Mayor superficie de mantenimiento cuando el contrato evoluciona

5. **Escenarios donde el adaptador no es suficiente**:
   - Cambio en el contrato de la interfaz (todos los implementadores deben actualizarse)
   - Cambio radical del modelo de dominio (`Account` → agregado complejo)
   - Operaciones que la interfaz no modela (bloqueo masivo, paginación)
   - Consistencia transaccional entre agregados (requiere Saga/Outbox, no adaptador)

---

## 3. Documento de Análisis ante Cambio de Prioridad ✅

**Ubicación**: `docs/parte3-cambio-prioridad.md`

### Contenido

1. **Análisis de Vulnerabilidades** del diseño actual:
   - CRÍTICA: Datos sin encriptar en BD
   - ALTA: No se verifica hash automáticamente
   - ALTA: Cache sin validación de integridad
   - MEDIA: Sin rate limiting
   - MEDIA: Basic Auth sin HTTPS obligatorio
   - BAJA: Auditoría incompleta

2. **Cambios Propuestos** (Seguridad como prioridad):
   - Encriptar campos sensibles (AES-256)
   - Verificación automática de integridad
   - Invalidar cache después de escrituras
   - Implementar rate limiting
   - Forzar HTTPS
   - Logging estructurado de seguridad

3. **Impacto Concreto en Rendimiento**:
   - Throughput: 500 → 350 tx/seg (-30%)
   - Latencia P95: 80ms → 180ms (+125%)
   - Latencia P99: 150ms → 350ms (+133%)
   - Reportes: 20ms → 200ms (+900%)

4. **Recomendación**: Implementar todos los cambios excepto invalidación agresiva de cache. Usar TTL de 30 segundos como compromiso.

---

## 4. Documento de Reflexión Técnica ✅

**Ubicación**: `docs/parte4-reflexion-tecnica.md`

### Contenido

1. **Decisiones Técnicas y Relación con Atributos**:
   - Stack tecnológico (Spring Boot + MongoDB)
   - Optimizaciones de rendimiento (índices, cache, pool)
   - Controles de seguridad (hash, validaciones, auditoría)

2. **Trade-offs Asumidos**:
   - Central: Rendimiento vs Seguridad
   - Secundario: Consistencia vs Disponibilidad (CAP)
   - Terciario: Complejidad vs Flexibilidad

3. **Riesgo Técnico Principal**:
   - #1 CRÍTICO: Datos sin encriptar en BD
   - #2 ALTO: Ausencia de rate limiting
   - #3 MEDIO: Cache puede quedar desactualizado

4. **Sostenibilidad del Diseño**:
   - Escalabilidad horizontal (5K → 50K usuarios)
   - Mantenibilidad (agregar tipos de transacciones)
   - Extensibilidad (nuevos atributos de calidad)
   - Evolución tecnológica (5 años)

5. **Roadmap de Mejora** (6 meses):
   - Mes 1-2: Seguridad crítica
   - Mes 3-4: Escalabilidad
   - Mes 5-6: Observabilidad

---

## Archivos Adicionales

### Documentación Operativa
- **README.md**: Guía completa del proyecto
- **QUICKSTART.md**: Inicio rápido con ejemplos
- **application-prod.properties.example**: Configuración de producción

### Código de Soporte
- **DataInitializer.java**: Datos de prueba para desarrollo
- **GlobalExceptionHandler.java**: Manejo de errores estandarizado

---

## Estructura de Directorios

```
TallerInicialSW2/

├── src/
│   ├── main/
│   │   ├── java/co/edu/uptc/taller/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── static/ (frontend)
│   │       └── application.properties
│   └── test/
├── README.md
├── QUICKSTART.md
└── pom.xml
```

---

## Cómo Ejecutar

1. **Levantar MongoDB**: `docker run -d -p 27017:27017 mongo:latest`
2. **Compilar**: `./mvnw clean install`
3. **Ejecutar**: `./mvnw spring-boot:run`
4. **Acceder**: 
   - Login: http://localhost:8080
   - Registro: http://localhost:8080/register.html
5. **Credenciales precargadas**: 
   - user1/password1
   - user2/password2
   - admin/admin123
6. **Registrar nuevos usuarios**: Acceder a la página de registro (sin autenticación requerida)

### Notas Importantes

- **Transacciones MongoDB deshabilitadas**: El sistema funciona con MongoDB standalone (sin replica set). Las anotaciones `@Transactional` están comentadas en `AuthServiceImpl` y `TransactionServiceImpl` para compatibilidad.
- **Módulo de autenticación**: Separado del sistema bancario principal con endpoints públicos en `/api/auth/**`
- **Interfaz de usuario**: Mensajes inline integrados en el flujo del documento (sin ventanas emergentes flotantes)

---

## Conclusiones

✅ **Todos los requerimientos funcionales implementados**
✅ **Balance entre rendimiento y seguridad documentado**
✅ **Trade-offs explícitos y justificados técnicamente**
✅ **Código funcional, documentado y organizado**
✅ **Documentación técnica completa y detallada**
✅ **Frontend básico y funcional**
✅ **Módulo de autenticación separado con registro de usuarios**
✅ **Compatible con MongoDB standalone (sin replica set)**

El sistema cumple con todos los objetivos de la práctica, demostrando:
- Capacidad de implementar sistemas con múltiples atributos de calidad
- Comprensión profunda de trade-offs arquitectónicos
- Toma de decisiones técnicas fundamentadas
- Adaptabilidad ante cambios de prioridades estratégicas
- Arquitectura modular con separación de responsabilidades (autenticación vs sistema bancario)
