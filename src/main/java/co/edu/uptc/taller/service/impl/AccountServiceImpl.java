package co.edu.uptc.taller.service.impl;

import co.edu.uptc.taller.Enums.AccountStatus;
import co.edu.uptc.taller.dto.CreateAccountRequest;
import co.edu.uptc.taller.model.Account;
import co.edu.uptc.taller.repository.AccountRepository;
import co.edu.uptc.taller.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Implementacion del servicio de cuentas.
 * Gestiona la creacion y consulta de cuentas bancarias.
 */
@Service
public class AccountServiceImpl implements AccountService {
    
    @Autowired
    private AccountRepository accountRepository;

    /**
     * Crea una nueva cuenta para un usuario.
     * Valida que el usuario no tenga ya una cuenta activa.
     * 
     * @param request Datos de la cuenta a crear
     * @return Cuenta creada
     * @throws RuntimeException si el usuario ya tiene una cuenta
     */
    @Override
    public Account createAccount(CreateAccountRequest request) {

        Optional<Account> existing = accountRepository.findByUserId(request.getUserId());
        if (existing.isPresent()) {
            throw new RuntimeException("ya existe una cuenta para ese userId: " + request.getUserId() + "");
        }

        String currency = request.getCurrency();
        if (currency == null || currency.trim().isEmpty()) {
            currency = "COP";
        }

        Account account = Account.builder()
                .userId(request.getUserId())
                .balance(request.getInitialBalance())
                .currency(currency)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return accountRepository.save(account);
    }

    /**
     * Obtiene una cuenta por su ID.
     * 
     * @param id ID de la cuenta
     * @return Cuenta encontrada
     * @throws RuntimeException si no existe
     */
    @Override
    public Account getAccountById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
    }
}
