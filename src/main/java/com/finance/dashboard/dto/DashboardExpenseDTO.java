package com.finance.dashboard.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardExpenseDTO {
    private String period;
    private BigDecimal totalAmount;
    private Integer transactionCount;
    private List<CategoryExpenseDTO> expensesByCategory;
}
