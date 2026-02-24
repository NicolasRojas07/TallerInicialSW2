# Guía de Inicio Rápido

## 1. Levantar MongoDB con Docker (Opcional)

Si no tienes MongoDB instalado localmente, puedes usar Docker:

```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

## 2. Compilar y ejecutar

```bash
./mvnw clean install
./mvnw spring-boot:run
```

## 3. Probar la API

### Crear una cuenta
```bash
curl -X POST http://localhost:8080/api/accounts \
  -u user1:password1 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user1",
    "initialBalance": 1000000,
    "currency": "COP"
  }'
```

Guarda el `id` de la cuenta retornado (ejemplo: `675a1b2c3d4e5f6789012345`)

### Procesar una transacción
```bash
curl -X POST http://localhost:8080/api/transactions \
  -u user1:password1 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user1",
    "accountId": "675a1b2c3d4e5f6789012345",
    "amount": 50000,
    "type": "TRANSFER",
    "description": "Transferencia de prueba"
  }'
```

### Obtener resumen de transacciones
```bash
curl -X GET http://localhost:8080/api/reports/user/user1/summary \
  -u user1:password1
```

## 4. Acceder al Frontend

Abre tu navegador en: `http://localhost:8080`

Credenciales:
- user1 / password1
- user2 / password2
- admin / admin123

## 5. Monitorear MongoDB

```bash
# Conectarse a MongoDB
mongosh

# Usar la base de datos
use taller_financiero

# Ver cuentas
db.accounts.find().pretty()

# Ver transacciones
db.transactions.find().pretty()

# Ver reglas de comisión
db.commission_rules.find().pretty()
```

## Troubleshooting

### Error: "Connection refused" en MongoDB
- Verifica que MongoDB esté ejecutándose: `mongod --version`
- Verifica la URL en `application.properties`

### Error: "Unauthorized" en API
- Verifica que estés enviando credenciales: `-u username:password`
- Usa uno de los usuarios configurados: user1, user2, admin

### Error: "Account not found"
- Crea primero una cuenta con `POST /api/accounts`
- Usa el ID retornado en las transacciones

### Frontend no carga
- Verifica que estés accediendo a `http://localhost:8080` (no 8080/static)
- Revisa la consola del navegador para errores de CORS
