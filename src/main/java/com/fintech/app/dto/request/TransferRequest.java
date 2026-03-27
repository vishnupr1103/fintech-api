package com.fintech.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "Receiver user ID is required")
    private Long receiverUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum transfer amount is 1.00")
    @DecimalMax(value = "100000.00", message = "Maximum transfer amount is 100000.00")
    private BigDecimal amount;

    private String description;
}
