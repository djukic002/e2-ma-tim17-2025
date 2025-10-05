package com.example.valorquest.model.dto;

public class UserItemDTO {
    private String id;
    private String equipmentId;
    private boolean activated;
    private int remainingBattles;
    private int upgradeLevel;
    private int reforgeLevel;
    private String attribute;
    private double bonus;
    private boolean isUpgradable;

    public UserItemDTO(String id, String equipmentId, boolean activated, int remainingBattles, int upgradeLevel, int reforgeLevel, String attribute, double bonus, boolean isUpgradable) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.activated = activated;
        this.remainingBattles = remainingBattles;
        this.upgradeLevel = upgradeLevel;
        this.reforgeLevel = reforgeLevel;
        this.attribute = attribute;
        this.bonus = bonus;
        this.isUpgradable = isUpgradable;
    }

    public UserItemDTO() {
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

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public int getRemainingBattles() {
        return remainingBattles;
    }

    public void setRemainingBattles(int remainingBattles) {
        this.remainingBattles = remainingBattles;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public void setUpgradeLevel(int upgradeLevel) {
        this.upgradeLevel = upgradeLevel;
    }

    public int getReforgeLevel() {
        return reforgeLevel;
    }

    public void setReforgeLevel(int reforgeLevel) {
        this.reforgeLevel = reforgeLevel;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public boolean isUpgradable() {
        return isUpgradable;
    }

    public void setUpgradable(boolean upgradable) {
        isUpgradable = upgradable;
    }
}
