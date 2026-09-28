package com.dharsan.expence.dto;

public record ExpenseItemRequest(
        String category,
        double amount
) {
}