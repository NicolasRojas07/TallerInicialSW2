# Sistema de Transacciones Financieras

Sistema backend desarrollado en Spring Boot para procesar transacciones financieras con enfoque en el balance entre rendimiento y seguridad.

## Contexto Académico

Este proyecto es parte de la **Práctica 1: Conflicto entre Rendimiento y Seguridad** del curso de Ingeniería de Software II. El objetivo es desarrollar un sistema funcional que cumpla simultáneamente con dos atributos de calidad: rendimiento y seguridad.

## Características Principales

### Requerimientos Funcionales Implementados
- ✅ Registrar transacciones (monto, usuario, tipo)
- ✅ Calcular comisión según tipo de transacción
- ✅ Validar saldo suficiente antes de procesar
- ✅ Persistir información de transacciones
- ✅ Generar resúmenes de transacciones

### Atributos de Calidad

#### Rendimiento
- Arquitectura optimizada para 5000 usuarios concurrentes
- Cache en memoria para cálculos frecuentes (comisiones, reportes)
- Índices en campos clave para consultas rápidas
- Connection pool configurado para alta concurrencia
- Sesiones stateless para escalabilidad horizontal

#### Seguridad
- Autenticación HTTP Basic en todos los endpoints
- Hash SHA-256 de integridad para cada transacción
- Validación estricta de saldo y permisos
- Auditoría básica (IP, sessionId, timestamps)
- Transacciones atómicas para consistencia

## Stack Tecnológico

- **Backend**: Spring Boot 4.0.2
- **Base de Datos**: MongoDB
- **Seguridad**: Spring Security
- **Cache**: Spring Cache (en memoria)
- **Build Tool**: Maven
- **Java**: 21
- **Frontend**: HTML5 + CSS3 + JavaScript (vanilla)

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/co/edu/uptc/taller/
│   │   ├── config/          # Configuraciones (Security, Cache, MongoDB)
│   │   ├── controller/      # Controladores REST
│   │   ├── dto/             # Objetos de transferencia de datos
│   │   ├── Enums/           # Enumeraciones
│   │   ├── model/           # Entidades de dominio
│   │   ├── repository/      # Repositorios MongoDB
│   │   └── service/         # Lógica de negocio
│   │       └── impl/        # Implementaciones de servicios
│   └── resources/
│       ├── static/          # Frontend (HTML, CSS, JS)
│       └── application.properties
└── test/
docs/                        # Documentación técnica
├── parte1-diseno-inicial.md
├── parte3-cambio-prioridad.md
└── parte4-reflexion-tecnica.md
```

## Requisitos Previos

1. Java 21 o superior
2. Maven 3.8+
3. MongoDB 4.4+ ejecutándose en `localhost:27017`

## Instalación y Ejecución

### 1. Clonar el repositorio
```bash
git clone <url-repositorio>
cd TallerInicialSW2
```

### 2. Configurar MongoDB
Asegúrate de que MongoDB esté ejecutándose:
```bash
mongod --dbpath /ruta/a/tu/data
```

### 3. Compilar el proyecto
```bash
./mvnw clean install
```

### 4. Ejecutar la aplicación
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## Uso del Sistema

### Frontend Web
Accede a `http://localhost:8080` en tu navegador.

**Usuarios de prueba**:
- Usuario: `user1` / Contraseña: `password1`
- Usuario: `user2` / Contraseña: `password2`
- Usuario: `admin` / Contraseña: `admin123`

### API REST

#### Autenticación
Todas las peticiones requieren HTTP Basic Authentication:
```bash
Authorization: Basic <base64(username:password)>
```

#### Endpoints Principales

**Cuentas**
```bash
# Crear cuenta
POST /api/accounts
Content-Type: application/json
{
  "userId": "user1",
  "initialBalance": 1000000,
  "currency": "COP"
}

# Obtener cuenta
GET /api/accounts/{accountId}
```

**Transacciones**
```bash
# Procesar transacción
POST /api/transactions
Content-Type: application/json
{
  "userId": "user1",
  "accountId": "<accountId>",
  "amount": 50000,
  "type": "TRANSFER",
  "description": "Pago de servicio"
}

# Consultar transacciones por usuario
GET /api/transactions/user/{userId}

# Consultar transacciones por cuenta
GET /api/transactions/account/{accountId}
```

**Reportes**
```bash
# Resumen de usuario
GET /api/reports/user/{userId}/summary

# Resumen de cuenta
GET /api/reports/account/{accountId}/summary
```

## Tipos de Transacciones y Comisiones

| Tipo | Comisión | Mínimo | Máximo |
|------|----------|--------|--------|
| TRANSFER | 0.5% | $500 | $10,000 |
| DEPOSIT | 0.2% | $200 | $5,000 |
| WITHDRAWAL | 0.7% | $700 | $15,000 |
| PAYMENT | 1.0% | $1,000 | $20,000 |

## Documentación Técnica

La documentación completa del diseño y análisis técnico se encuentra en:

- **[Parte 1: Diseño Inicial](docs/parte1-diseno-inicial.md)**: Decisiones de arquitectura, priorización de atributos y trade-offs iniciales.
- **[Parte 3: Cambio de Prioridad](docs/parte3-cambio-prioridad.md)**: Análisis de vulnerabilidades y cambios propuestos al priorizar seguridad.
- **[Parte 4: Reflexión Técnica](docs/parte4-reflexion-tecnica.md)**: Trade-offs asumidos, riesgos y sostenibilidad del diseño.

## Trade-offs Principales

### Rendimiento Priorizado
- ✅ Cache agresivo → Reportes rápidos (20ms)
- ✅ Índices optimizados → Consultas O(log n)
- ✅ Sesiones stateless → Escalabilidad horizontal
- ⚠️ Datos sin encriptar → Riesgo de exposición

### Seguridad Garantizada
- ✅ Hash de integridad → Detecta manipulación
- ✅ Validaciones estrictas → Previene fraude
- ✅ Transacciones atómicas → Consistencia
- ⚠️ Costo computacional → -20% throughput

## Métricas de Rendimiento

**Configuración actual (prioridad: rendimiento)**:
- Throughput: ~500 transacciones/segundo
- Latencia P95: <100ms
- Latencia P99: <200ms
- Tiempo de reportes: 20ms (con cache)

## Limitaciones Conocidas

1. **Datos sin encriptar en BD**: Requiere implementar CSFLE
2. **Sin rate limiting**: Vulnerable a flood attacks
3. **Cache no distribuido**: No funciona con múltiples instancias
4. **Usuarios en memoria**: No escala más allá de pruebas

## Roadmap

### Corto Plazo (1-2 meses)
- [ ] Encriptación de campos sensibles
- [ ] Rate limiting con Bucket4j
- [ ] Verificación automática de integridad

### Mediano Plazo (3-6 meses)
- [ ] Cache distribuido (Redis)
- [ ] OAuth2 para autenticación
- [ ] Observabilidad (Prometheus + Grafana)

## Autor

Proyecto desarrollado como parte del curso de Ingeniería de Software II - UPTC.

## Licencia

Este proyecto es de uso académico.
