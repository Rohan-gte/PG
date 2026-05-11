package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    private String purpose;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenantToVisit;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    private String idProofType;
    private String idProofNumber;
}
