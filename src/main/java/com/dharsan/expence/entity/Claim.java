package com.dharsan.expence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    @ManyToOne
    private Employee employee;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    private List<ExpenseItem> items;

    private double totalAmount;

    private String paymentReference;
}
