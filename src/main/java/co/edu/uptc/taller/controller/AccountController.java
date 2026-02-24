package co.edu.uptc.taller.controller;

import co.edu.uptc.taller.dto.AccountResponse;
import co.edu.uptc.taller.dto.CreateAccountRequest;
import co.edu.uptc.taller.model.Account;
import co.edu.uptc.taller.service.AccountService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestion de cuentas bancarias.
 * Expone endpoints para crear y consultar cuentas.
 */
@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Crea una nueva cuenta bancaria.
     * Validaciones automaticas via Bean Validation.
     * 
     * @param request Datos de la cuenta a crear
     * @return Cuenta creada con codigo 201
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request);
        AccountResponse response = convertToResponse(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene una cuenta por su ID.
     * 
     * @param id ID de la cuenta
     * @return Cuenta encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String id) {
        Account account = accountService.getAccountById(id);
        AccountResponse response = convertToResponse(account);
        return ResponseEntity.ok(response);
    }

    /**
     * Convierte Account a AccountResponse.
     * 
     * @param account Entidad a convertir
     * @return DTO de respuesta
     */
    private AccountResponse convertToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
