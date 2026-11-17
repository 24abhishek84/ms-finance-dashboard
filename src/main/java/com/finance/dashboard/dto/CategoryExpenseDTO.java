package com.finance.dashboard.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseDTO {
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private BigDecimal totalAmount;
    private Integer transactionCount;
}
