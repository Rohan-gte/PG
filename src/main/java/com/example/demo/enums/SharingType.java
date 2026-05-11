package com.example.demo.enums;

public enum SharingType {
    ONE(1),
    TWO(2),
    THREE(3);

    private final int capacity;

    SharingType(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public static SharingType fromCapacity(int capacity) {
        for (SharingType st : values()) {
            if (st.capacity == capacity) return st;
        }
        throw new IllegalArgumentException("Invalid sharing capacity: " + capacity);
    }
}
