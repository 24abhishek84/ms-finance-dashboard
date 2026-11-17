package com.finance.dashboard.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodExpenseDTO {
    private String period;
    private BigDecimal totalExpense;
    private Integer transactionCount;
    private java.util.List<CategoryExpenseDTO> categoryBreakdown;
}
