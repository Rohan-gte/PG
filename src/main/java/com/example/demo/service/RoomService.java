package com.example.demo.service;

import com.example.demo.model.Room;
import com.example.demo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAvailableRooms() {
        return roomRepository.findAll().stream()
                .filter(room -> room.getOccupiedBeds() < room.getCapacity())
                .collect(Collectors.toList());
    }

    public Room allocateBed(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (room.getOccupiedBeds() < room.getCapacity()) {
            room.setOccupiedBeds(room.getOccupiedBeds() + 1);
            return roomRepository.save(room);
        } else {
            throw new RuntimeException("Room is already full");
        }
    }

    public void deallocateBed(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (room.getOccupiedBeds() > 0) {
            room.setOccupiedBeds(room.getOccupiedBeds() - 1);
            roomRepository.save(room);
        }
    }
}
