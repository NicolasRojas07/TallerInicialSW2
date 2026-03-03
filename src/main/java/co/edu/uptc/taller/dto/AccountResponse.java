package co.edu.uptc.taller.dto;

import co.edu.uptc.taller.Enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String id;
    private String userId;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
