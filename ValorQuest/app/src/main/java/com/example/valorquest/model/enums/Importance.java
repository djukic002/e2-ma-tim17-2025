package com.example.valorquest.model.enums;

public enum Importance {
    LOW(1), // normalan 1 xp
    MEDIUM(3), // vazan 3 xp
    HIGH(10), // ekstra vazan 10 xp
    SPECIAL(100); // specijalan 100 xp

    private final int baseXP;

    Importance(int baseXP) {
        this.baseXP = baseXP;
    }

    public int getBaseXP() {
        return baseXP;
    }
}
