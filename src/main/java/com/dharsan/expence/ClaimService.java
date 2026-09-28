package com.dharsan.expence;

import com.dharsan.expence.dto.CreateClaimRequest;
import com.dharsan.expence.dto.ExpenseItemRequest;
import com.dharsan.expence.dto.ManagerDecisionRequest;
import com.dharsan.expence.dto.PaymentRequest;
import com.dharsan.expence.entity.ApprovalStep;
import com.dharsan.expence.entity.Claim;
import com.dharsan.expence.entity.Employee;
import com.dharsan.expence.entity.ExpenseItem;
import com.dharsan.expence.repository.ApprovalStepRepository;
import com.dharsan.expence.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ClaimService {

    private static final Map<String, Double> POLICY_LIMITS = Map.of(
            "TRAVEL", 500.0,
            "MEALS", 75.0,
            "ACCOMMODATION", 250.0,
            "OTHER", 100.0
    );

    private final ClaimRepository claimRepository;
    private final EmployeeRepository employeeRepository;
    private final ApprovalStepRepository approvalStepRepository;

    public ClaimService(ClaimRepository claimRepository, EmployeeRepository employeeRepository,
                        ApprovalStepRepository approvalStepRepository) {
        this.claimRepository = claimRepository;
        this.employeeRepository = employeeRepository;
        this.approvalStepRepository = approvalStepRepository;
    }

    @Transactional
    public Claim submit(CreateClaimRequest request) {
        validateSubmission(request);
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ClaimRuleException("Employee " + request.employeeId() + " was not found"));

        Claim claim = new Claim();
        claim.setEmployee(employee);
        claim.setStatus(statusFor(request.items()));
        claim.setItems(request.items().stream().map(item -> toEntity(item, claim)).toList());
        claim.setTotalAmount(request.items().stream().mapToDouble(ExpenseItemRequest::amount).sum());
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim decide(Long claimId, ManagerDecisionRequest request) {
        if (request == null || request.remarks() == null || request.remarks().isBlank()) {
            throw new ClaimRuleException("remarks are required for a manager decision");
        }
        Claim claim = findClaim(claimId);
        requireStatus(claim, ClaimStatus.SUBMITTED.name(), ClaimStatus.NEEDS_OVERRIDE.name());
        boolean hasExceededItem = claim.getItems().stream().anyMatch(ExpenseItem::isExceedsLimit);
        if (hasExceededItem && request.approved() && !request.overridePolicyLimit()) {
            throw new ClaimRuleException("A claim with an over-limit item requires a manager override before approval");
        }

        claim.setStatus(request.approved() ? ClaimStatus.MANAGER_APPROVED.name() : ClaimStatus.MANAGER_REJECTED.name());
        saveApprovalStep(claim, "MANAGER", request.approved() ? "APPROVED" : "REJECTED", request.remarks());
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim pay(Long claimId, PaymentRequest request) {
        if (request == null || request.paymentReference() == null || request.paymentReference().isBlank()) {
            throw new ClaimRuleException("paymentReference is required");
        }
        Claim claim = findClaim(claimId);
        requireStatus(claim, ClaimStatus.MANAGER_APPROVED.name());
        claim.setPaymentReference(request.paymentReference().trim());
        claim.setStatus(ClaimStatus.PAID.name());
        saveApprovalStep(claim, "FINANCE", "PAID", "Payment recorded with reference " + request.paymentReference().trim());
        return claimRepository.save(claim);
    }

    public List<Claim> findAll() {
        return claimRepository.findAll();
    }

    public List<Claim> findPending(String stage) {
        if (stage == null || stage.isBlank()) {
            return claimRepository.findByStatus(ClaimStatus.SUBMITTED.name());
        }
        return switch (stage.trim().toUpperCase(Locale.ROOT)) {
            case "MANAGER", "SUBMITTED" -> claimRepository.findByStatus(ClaimStatus.SUBMITTED.name());
            case "OVERRIDE" -> claimRepository.findByStatus(ClaimStatus.NEEDS_OVERRIDE.name());
            case "FINANCE", "MANAGER_APPROVED" -> claimRepository.findByStatus(ClaimStatus.MANAGER_APPROVED.name());
            default -> throw new ClaimRuleException("Unknown approval stage: " + stage);
        };
    }

    private ExpenseItem toEntity(ExpenseItemRequest request, Claim claim) {
        String category = request.category().trim().toUpperCase(Locale.ROOT);
        double limit = POLICY_LIMITS.getOrDefault(category, POLICY_LIMITS.get("OTHER"));
        ExpenseItem item = new ExpenseItem();
        item.setCategory(category);
        item.setAmount(request.amount());
        item.setPolicyLimit(limit);
        item.setExceedsLimit(request.amount() > limit);
        item.setClaim(claim);
        return item;
    }

    private String statusFor(List<ExpenseItemRequest> items) {
        return items.stream().anyMatch(item -> {
            String category = item.category().trim().toUpperCase(Locale.ROOT);
            return item.amount() > POLICY_LIMITS.getOrDefault(category, POLICY_LIMITS.get("OTHER"));
        }) ? ClaimStatus.NEEDS_OVERRIDE.name() : ClaimStatus.SUBMITTED.name();
    }

    private void validateSubmission(CreateClaimRequest request) {
        if (request == null || request.employeeId() == null) {
            throw new ClaimRuleException("employeeId is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new ClaimRuleException("at least one expense item is required");
        }
        for (ExpenseItemRequest item : request.items()) {
            if (item == null || item.category() == null || item.category().isBlank()) {
                throw new ClaimRuleException("each expense item requires a category");
            }
            if (item.amount() <= 0) {
                throw new ClaimRuleException("each expense item amount must be greater than zero");
            }
        }
    }

    private Claim findClaim(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimRuleException("Claim " + claimId + " was not found"));
    }

    private void requireStatus(Claim claim, String... allowedStatuses) {
        for (String allowedStatus : allowedStatuses) {
            if (allowedStatus.equals(claim.getStatus())) {
                return;
            }
        }
        throw new ClaimRuleException("Claim " + claim.getId() + " cannot be changed from status " + claim.getStatus());
    }

    private void saveApprovalStep(Claim claim, String stage, String decision, String remarks) {
        ApprovalStep step = new ApprovalStep();
        step.setClaim(claim);
        step.setStage(stage);
        step.setDecision(decision);
        step.setRemarks(remarks);
        step.setActionedAt(LocalDateTime.now());
        approvalStepRepository.save(step);
    }
}