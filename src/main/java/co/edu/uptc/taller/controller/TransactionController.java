package co.edu.uptc.taller.controller;

import co.edu.uptc.taller.dto.CreateTransactionRequest;
import co.edu.uptc.taller.dto.TransactionResponse;
import co.edu.uptc.taller.model.Transaction;
import co.edu.uptc.taller.service.TransactionService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestion de transacciones financieras.
 * Expone endpoints para procesar y consultar transacciones.
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * Procesa una nueva transaccion financiera.
     * Captura IP del cliente para auditoria - Atributo de Seguridad.
     * Validaciones automaticas via Bean Validation.
     * 
     * @param request Datos de la transaccion
     * @param httpRequest Request HTTP para obtener IP
     * @return Transaccion procesada con codigo 201
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            HttpServletRequest httpRequest) {
        
        // Captura IP del cliente para auditoria
        String ipAddress = getClientIpAddress(httpRequest);
        request.setIpAddress(ipAddress);
        
        // Captura session ID si existe
        if (request.getSessionId() == null) {
            request.setSessionId(httpRequest.getSession().getId());
        }
        
        TransactionResponse response = transactionService.processTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene una transaccion por su ID.
     * 
     * @param id ID de la transaccion
     * @return Transaccion encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
        Transaction transaction = transactionService.getTransactionById(id);
        TransactionResponse response = convertToResponse(transaction);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todas las transacciones de un usuario.
     * 
     * @param userId ID del usuario
     * @return Lista de transacciones del usuario
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByUser(@PathVariable String userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        List<TransactionResponse> responses = transactions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene todas las transacciones de una cuenta.
     * 
     * @param accountId ID de la cuenta
     * @return Lista de transacciones de la cuenta
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable String accountId) {
        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);
        List<TransactionResponse> responses = transactions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene la IP real del cliente considerando proxies y balanceadores.
     * Para auditoria y seguridad.
     * 
     * @param request Request HTTP
     * @return IP del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Convierte Transaction a TransactionResponse.
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
