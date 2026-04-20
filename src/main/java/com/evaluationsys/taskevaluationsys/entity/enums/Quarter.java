package com.evaluationsys.taskevaluationsys.entity.enums;

public enum Quarter {

    Q1(1),
    Q2(2),
    Q3(3),
    Q4(4);

    private final int value;

    Quarter(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Convert integer to Quarter enum safely.
     * Example: 1 → Q1
     */
    public static Quarter fromInt(int value) {
        for (Quarter q : Quarter.values()) {
            if (q.value == value) {
                return q;
            }
        }
        throw new IllegalArgumentException("Invalid quarter value: " + value);
    }
}