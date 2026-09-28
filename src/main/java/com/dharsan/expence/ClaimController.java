package com.dharsan.expence;

import com.dharsan.expence.dto.CreateClaimRequest;
import com.dharsan.expence.dto.ManagerDecisionRequest;
import com.dharsan.expence.dto.PaymentRequest;
import com.dharsan.expence.entity.Claim;
import com.dharsan.expence.entity.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClaimController {

    private final ClaimService claimService;
    private final EmployeeRepository employeeRepository;

    public ClaimController(ClaimService claimService, EmployeeRepository employeeRepository) {
        this.claimService = claimService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/employees")
    public List<Employee> employees() {
        return employeeRepository.findAll();
    }

    @PostMapping("/claims")
    @ResponseStatus(HttpStatus.CREATED)
    public Claim submit(@RequestBody CreateClaimRequest request) {
        return claimService.submit(request);
    }

    @GetMapping("/claims")
    public List<Claim> claims() {
        return claimService.findAll();
    }

    @GetMapping("/claims/pending")
    public List<Claim> pending(@RequestParam(required = false) String stage) {
        return claimService.findPending(stage);
    }

    @PatchMapping("/claims/{claimId}/manager-decision")
    public Claim managerDecision(@PathVariable Long claimId, @RequestBody ManagerDecisionRequest request) {
        return claimService.decide(claimId, request);
    }

    @PatchMapping("/claims/{claimId}/payment")
    public Claim payment(@PathVariable Long claimId, @RequestBody PaymentRequest request) {
        return claimService.pay(claimId, request);
    }
}