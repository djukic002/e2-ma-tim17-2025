package com.example.valorquest.model;

public class UserItem {
    private String id;
    private String equipmentId;
    private int remainingBattles;
    private int reforgeLevel;
    private int upgradeLevel;
    public UserItem(){}
    public UserItem(String id, String equipmentId, int remainingBattles, int reforgeLevel, int upgradeLevel) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.remainingBattles = remainingBattles;
        this.reforgeLevel = reforgeLevel;
        this.upgradeLevel = upgradeLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getRemainingBattles() {
        return remainingBattles;
    }

    public void setRemainingBattles(int remainingBattles) {
        this.remainingBattles = remainingBattles;
    }

    public int getReforgeLevel() {
        return reforgeLevel;
    }

    public void setReforgeLevel(int reforgeLevel) {
        this.reforgeLevel = reforgeLevel;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public void setUpgradeLevel(int upgradeLevel) {
        this.upgradeLevel = upgradeLevel;
    }
}
