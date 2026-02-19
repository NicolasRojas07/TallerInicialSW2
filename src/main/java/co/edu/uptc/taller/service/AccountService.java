package co.edu.uptc.taller.service;

import co.edu.uptc.taller.Enums.AccountStatus;
import co.edu.uptc.taller.dto.CreateAccountRequest;
import co.edu.uptc.taller.model.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    Account createAccount(CreateAccountRequest request);
    Account getAccountById(String id);
}
