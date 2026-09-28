package com.dharsan.expence.repository;

import com.dharsan.expence.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
}