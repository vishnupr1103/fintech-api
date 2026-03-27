package com.fintech.app.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountResponse {
    private Long accountId;
    private Long userId;
    private String userName;
    private BigDecimal balance;
}
