package com.dharsan.expence.repository;

import com.dharsan.expence.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByStatus(String status);

    List<Claim> findByEmployeeId(Long employeeId);
}