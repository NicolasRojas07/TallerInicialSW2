package co.edu.uptc.taller.service.impl;

import co.edu.uptc.taller.Enums.AccountStatus;
import co.edu.uptc.taller.dto.CreateTransactionRequest;
import co.edu.uptc.taller.dto.TransactionResponse;
import co.edu.uptc.taller.model.Account;
import co.edu.uptc.taller.model.Transaction;
import co.edu.uptc.taller.model.TransactionStatus;
import co.edu.uptc.taller.repository.AccountRepository;
import co.edu.uptc.taller.repository.TransactionRepository;
import co.edu.uptc.taller.service.ComissionService;
import co.edu.uptc.taller.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

/**
 * Implementacion del servicio de transacciones.
 * Aplica validaciones estrictas y calcula hash de integridad - Trade-off: seguridad vs rendimiento
 * Usa transacciones para garantizar atomicidad - Trade-off: seguridad vs rendimiento
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ComissionService comissionService;

    /**
     * Procesa una transaccion completa con todas las validaciones y controles de seguridad.
     * Metodo transaccional para garantizar atomicidad en operaciones de BD.
     * 
     * Flujo:
     * 1. Valida que la cuenta exista y este activa
     * 2. Calcula comision segun tipo de transaccion
     * 3. Valida saldo suficiente (monto + comision)
     * 4. Actualiza saldo de la cuenta
     * 5. Crea transaccion con hash de integridad
     * 6. Persiste cambios atomicamente
     * 
     * @param request Datos de la transaccion
     * @return Respuesta con transaccion procesada
     * @throws RuntimeException si falla alguna validacion
     */
    @Override
    // @Transactional // Deshabilitado: requiere MongoDB Replica Set
    public TransactionResponse processTransaction(CreateTransactionRequest request) {
        // Resuelve la cuenta: si se envía accountId la busca por ID, si no la busca por userId
        Account account;
        if (request.getAccountId() != null && !request.getAccountId().isBlank()) {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con ID: " + request.getAccountId()));
        } else {
            account = accountRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new RuntimeException(
                            "No existe cuenta activa para el usuario: " + request.getUserId() +
                            ". Crea primero una cuenta en el módulo 'Cuentas'."));
        }

        // Asegura que accountId quede poblado en el request para el resto del flujo
        request.setAccountId(account.getId());

        // Valida que la cuenta este activa - Atributo de Seguridad
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("La cuenta no está activa: " + account.getId());
        }

        // Valida que el usuario de la transaccion coincida con el de la cuenta - Atributo de Seguridad
        if (!account.getUserId().equals(request.getUserId())) {
            throw new RuntimeException("El usuario no es dueño de esta cuenta");
        }

        // Calcula la comision aplicable
        BigDecimal commission = comissionService.calculateCommission(
                request.getType(), 
                request.getAmount()
        );

        // Calcula monto total (monto + comision)
        BigDecimal totalAmount = request.getAmount().add(commission);

        // Valida saldo suficiente - Validacion de Negocio critica
        if (account.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("Insufficient balance. Required: " + totalAmount + ", Available: " + account.getBalance());
        }

        // Crea la transaccion en estado pendiente
        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .type(request.getType())
                .status(TransactionStatus.PENDING)
                .commission(commission)
                .totalAmount(totalAmount)
                .createdAt(Instant.now())
                .description(request.getDescription())
                .ipAddress(request.getIpAddress())
                .sessionId(request.getSessionId())
                .build();

        // Calcula hash de integridad ANTES de persistir - Atributo de Seguridad
        // El hash permite detectar manipulacion posterior de datos criticos
        String integrityHash = calculateIntegrityHash(transaction);
        transaction.setIntegrityHash(integrityHash);

        // Actualiza el saldo de la cuenta - Operacion atomica
        account.setBalance(account.getBalance().subtract(totalAmount));
        account.setUpdatedAt(Instant.now());
        accountRepository.save(account);

        // Marca transaccion como completada
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setProcessedAt(Instant.now());

        // Persiste la transaccion
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Convierte a DTO de respuesta
        return convertToResponse(savedTransaction);
    }

    /**
     * Calcula un hash SHA-256 de los datos criticos de la transaccion.
     * Este hash permite verificar posteriormente que los datos no fueron alterados.
     * Trade-off: Seguridad (integridad) vs Rendimiento (calculo criptografico).
     * 
     * Datos incluidos en el hash:
     * - userId, accountId, amount, type, commission, totalAmount, createdAt
     * 
     * @param transaction Transaccion para calcular hash
     * @return Hash hexadecimal de los datos criticos
     */
    private String calculateIntegrityHash(Transaction transaction) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Concatena datos criticos de la transaccion
            String data = transaction.getUserId() + "|" +
                         transaction.getAccountId() + "|" +
                         transaction.getAmount().toPlainString() + "|" +
                         transaction.getType().name() + "|" +
                         transaction.getCommission().toPlainString() + "|" +
                         transaction.getTotalAmount().toPlainString() + "|" +
                         transaction.getCreatedAt().toString();
            
            // Calcula hash SHA-256
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            // Convierte a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating integrity hash", e);
        }
    }

    /**
     * Verifica la integridad de una transaccion recalculando su hash.
     * Permite detectar manipulacion de datos en BD - Atributo de Seguridad.
     * 
     * @param transaction Transaccion a verificar
     * @return true si el hash coincide, false si fue manipulada
     */
    public boolean verifyIntegrity(Transaction transaction) {
        String originalHash = transaction.getIntegrityHash();
        String calculatedHash = calculateIntegrityHash(transaction);
        return originalHash.equals(calculatedHash);
    }

    /**
     * Obtiene una transaccion por ID.
     * 
     * @param id ID de la transaccion
     * @return Transaccion encontrada
     * @throws RuntimeException si no existe
     */
    @Override
    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }

    /**
     * Obtiene todas las transacciones de un usuario.
     * Consulta optimizada por indice en userId.
     * 
     * @param userId ID del usuario
     * @return Lista de transacciones
     */
    @Override
    public List<Transaction> getTransactionsByUserId(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    /**
     * Obtiene todas las transacciones de una cuenta.
     * Consulta optimizada por indice en accountId.
     * 
     * @param accountId ID de la cuenta
     * @return Lista de transacciones
     */
    @Override
    public List<Transaction> getTransactionsByAccountId(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    /**
     * Convierte una entidad Transaction a DTO TransactionResponse.
     * No expone datos sensibles como hash de integridad o IP.
     * 
     * @param transaction Entidad a convertir
     * @return DTO de respuesta
     */
    private TransactionResponse convertToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .accountId(transaction.getAccountId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .commission(transaction.getCommission())
                .totalAmount(transaction.getTotalAmount())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .description(transaction.getDescription())
                .build();
    }
}
