package com.example.valorquest.model.enums;

public enum Difficulty {
    NOVICE(1), //veoma lak 1 xp
    ADVENTURER(3), // lak 3 xp
    VETERAN(7), // tezak 7 xp
    LEGENDARY(20); // ekstremno tezak 20 xp

    private final int baseXP;

    Difficulty(int baseXP) {
        this.baseXP = baseXP;
    }

    public int getBaseXP() {
        return baseXP;
    }
}
