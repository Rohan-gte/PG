package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDate joiningDate;
    private LocalDate agreementExpiryDate;
    private boolean isDocumentsVerified;
    
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING_APPROVAL, ACTIVE, EXPIRED, LEFT
    }
}
