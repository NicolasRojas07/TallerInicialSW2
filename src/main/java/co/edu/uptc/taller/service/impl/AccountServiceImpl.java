package co.edu.uptc.taller.service.impl;

import co.edu.uptc.taller.Enums.AccountStatus;
import co.edu.uptc.taller.dto.CreateAccountRequest;
import co.edu.uptc.taller.model.Account;
import co.edu.uptc.taller.repository.AccountRepository;
import co.edu.uptc.taller.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service

public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepository accountRepository;





    @Override
    public Account createAccount(CreateAccountRequest request) {

        var existing = accountRepository.findByUserId(request.getUserId());
        if (existing.isPresent()) {
            throw new RuntimeException("ya existe una cuenta para ese userId: " + request.getUserId() + "");
        }

        String currency = request.getCurrency();
        if (currency == null || currency.isBlank()) {
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


    @Override
    public Account getAccountById(String id) {
        return null;
    }
}
