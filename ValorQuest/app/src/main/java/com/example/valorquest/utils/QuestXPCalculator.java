package com.example.valorquest.utils;

import com.example.valorquest.model.enums.Difficulty;
import com.example.valorquest.model.enums.Importance;

public class QuestXPCalculator {
    private static int getDifficultyXP(Difficulty difficulty, int level) {
        int XP = difficulty.getBaseXP();
        for (int i = 1; i <= level; i++) {
            XP = XP + (int) Math.round(XP / 2.0);
        }
        return XP;
    }

    private static int getImportanceXP(Importance importance, int level) {
        int XP = importance.getBaseXP();
        for (int i = 1; i <= level; i++) {
            XP = XP + (int) Math.round(XP / 2.0);
        }
        return XP;
    }

    public static int getQuestXP(Difficulty difficulty, Importance importance, int level) {
        return getDifficultyXP(difficulty, level) + getImportanceXP(importance, level);
    }
}
