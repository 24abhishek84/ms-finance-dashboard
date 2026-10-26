package com.finance.dashboard.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    
    @NotBlank(message = "Description is required")
    @Size(max = 255)
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Amount exceeds maximum")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in future")
    private LocalDate transactionDate;
    
    @Size(max = 500)
    private String notes;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
