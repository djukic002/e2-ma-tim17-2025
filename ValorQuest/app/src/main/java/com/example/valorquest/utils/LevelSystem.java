package com.example.valorquest.utils;

public class LevelSystem {
    public static int getXPForLevel(int level) {
        if (level <= 1)
            return 200;

        int XPPrev = getXPForLevel(level - 1);
        double rawValue = XPPrev * 2 + (XPPrev / 2.0);
        return roundUpToHundred(rawValue);
    }

    public static int getPPForLevel(int level) {
        if (level <= 1)
            return 40;

        int PPPrev = getPPForLevel(level - 1);
        double rawValue = PPPrev + 0.75 * PPPrev;
        return (int) Math.round(rawValue);
    }

    private static int roundUpToHundred(double value) {
        return ((int) Math.ceil(value / 100.0)) * 100;
    }

    public static String getTitle(int level) {
        switch(level) {
            case 0: return "Apprentice";
            case 1: return "Acolyte";
            case 2: return "Adventurer";
            case 3: return "Champion";
            case 4: return "Warlord";
            case 5: return "Conqueror";
            case 6: return "Legend";
            case 7: return "Mythic";
            case 8: return "Elder";
            case 9: return "Immortal";
            default: return "Eternal";
        }
    }
}
