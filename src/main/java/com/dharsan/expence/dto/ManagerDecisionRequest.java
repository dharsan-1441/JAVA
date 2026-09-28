package com.dharsan.expence.dto;

public record ManagerDecisionRequest(
        boolean approved,
        boolean overridePolicyLimit,
        String remarks
) {
}