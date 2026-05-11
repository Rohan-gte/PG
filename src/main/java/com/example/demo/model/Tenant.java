package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
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

    public Tenant() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public LocalDate getAgreementExpiryDate() {
        return agreementExpiryDate;
    }

    public void setAgreementExpiryDate(LocalDate agreementExpiryDate) {
        this.agreementExpiryDate = agreementExpiryDate;
    }

    public boolean isDocumentsVerified() {
        return isDocumentsVerified;
    }

    // Matches your service call: tenant.setDocumentsVerified(true/false)
    public void setDocumentsVerified(boolean documentsVerified) {
        isDocumentsVerified = documentsVerified;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
