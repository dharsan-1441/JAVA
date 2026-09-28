package com.dharsan.expence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stage;
    private String decision;
    private String remarks;

    private LocalDateTime actionedAt;

    @ManyToOne
    @JsonIgnore
    private Claim claim;
}
