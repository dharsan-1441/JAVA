package com.dharsan.expence.dto;

import java.util.List;

public record CreateClaimRequest(
        Long employeeId,
        List<ExpenseItemRequest> items
) {
}