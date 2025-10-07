package com.example.valorquest.model;

import com.example.valorquest.model.enums.BossStatus;

import java.util.UUID;

public class Boss {
    private String id;
    private String userId;
    private String name;
    private int level;
    private int originalHp;
    private int currentHp;
    private double hitChance;
    private int attackLimit;
    private int attacksRemaining;
    private BossStatus status; // CREATED | BATTLE | DEFEATED | FAILED
    private int goldReward;
    public Boss() {
    }

    // creating a first boss
    public Boss(String userId, String name, double hitChance) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.name = name;
        this.level = 0;
        this.originalHp = 200;
        this.currentHp = 200;
        this.hitChance = hitChance;
        this.attackLimit = 5;
        this.attacksRemaining = 5;
        this.status = BossStatus.ACTIVE;
        this.goldReward = 200;
    }

    // creating a boss from previous max boss
    public Boss(Boss boss, double hitChance) {
        this.id = UUID.randomUUID().toString();
        this.userId = boss.getUserId();
        this.name = boss.getName();
        this.level = boss.getLevel() + 1;
        this.originalHp = boss.getOriginalHp() * 2 + boss.getOriginalHp() / 2;
        this.currentHp = originalHp;
        this.hitChance = hitChance;
        this.attackLimit = 5;
        this.attacksRemaining = 5;
        this.status = BossStatus.ACTIVE;
        this.goldReward = boss.getGoldReward() + boss.getGoldReward() / 5;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getOriginalHp() {
        return originalHp;
    }

    public void setOriginalHp(int originalHp) {
        this.originalHp = originalHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public double getHitChance() {
        return hitChance;
    }

    public void setHitChance(double hitChance) {
        this.hitChance = hitChance;
    }

    public int getAttackLimit() {
        return attackLimit;
    }

    public void setAttackLimit(int attackLimit) {
        this.attackLimit = attackLimit;
    }

    public int getAttacksRemaining() {
        return attacksRemaining;
    }

    public void setAttacksRemaining(int attacksRemaining) {
        this.attacksRemaining = attacksRemaining;
    }

    public BossStatus getStatus() {
        return status;
    }

    public void setStatus(BossStatus status) {
        this.status = status;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public void setGoldReward(int goldReward) {
        this.goldReward = goldReward;
    }
}
