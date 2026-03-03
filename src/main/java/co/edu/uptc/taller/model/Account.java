package co.edu.uptc.taller.model;

import co.edu.uptc.taller.Enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")

public class Account {

    @Id
    private String id;

    private String userId;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
