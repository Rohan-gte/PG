package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomNumber;

    private int capacity;
    private int occupiedBeds;
    private double price;

    @ManyToOne
    @JoinColumn(name = "floor_id")
    private Floor floor;

    public boolean isFull() {
        return occupiedBeds >= capacity;
    }
}
