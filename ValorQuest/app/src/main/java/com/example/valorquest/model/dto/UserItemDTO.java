package com.example.valorquest.model.dto;

import com.example.valorquest.model.Equipment;
import com.example.valorquest.model.UserItem;

public class UserItemDTO {
    private String id;
    private String equipmentId;
    private String equipmentName;
    private String equipmentType;
    private String attribute;
    private double bonus;
    private boolean activated;
    private int remainingBattles;
    private int reforgeLevel;
    private int upgradeLevel;
    private String description;
    private boolean isUpgradable;
    private int actualPrice;

    public UserItemDTO() {}

    public UserItemDTO(UserItem userItem, Equipment equipment) {
        this.id = userItem.getId();
        this.equipmentId = userItem.getEquipmentId();
        this.equipmentName = equipment.getName();
        this.equipmentType = equipment.getType();
        this.attribute = equipment.getAttribute();

        this.activated = userItem.isActivated();
        this.remainingBattles = userItem.getRemainingBattles();
        this.reforgeLevel = userItem.getReforgeLevel();
        this.upgradeLevel = userItem.getUpgradeLevel();
        this.description = equipment.getDescription();
        this.isUpgradable = equipment.isUpgradable();
        this.actualPrice = 0; // Will be set separately
        this.bonus = equipment.getBonus() + 0.02 * userItem.getReforgeLevel() + 0.01 * userItem.getUpgradeLevel();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }

    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public double getBonus() { return bonus; }
    public void setBonus(double bonus) { this.bonus = bonus; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    public int getRemainingBattles() { return remainingBattles; }
    public void setRemainingBattles(int remainingBattles) { this.remainingBattles = remainingBattles; }

    public int getReforgeLevel() { return reforgeLevel; }
    public void setReforgeLevel(int reforgeLevel) { this.reforgeLevel = reforgeLevel; }

    public int getUpgradeLevel() { return upgradeLevel; }
    public void setUpgradeLevel(int upgradeLevel) { this.upgradeLevel = upgradeLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isUpgradable() { return isUpgradable; }
    public void setUpgradable(boolean upgradable) { isUpgradable = upgradable; }

    public int getActualPrice() { return actualPrice; }
    public void setActualPrice(int actualPrice) { this.actualPrice = actualPrice; }

    // Helper methods
    public boolean isPermanent() {
        return remainingBattles < 0;
    }

    public String getBonusText() {
        return attribute + " +" + String.valueOf(Math.round(bonus * 100)) + "%";
    }

    public String getRemainingBattlesText() {
        if (isPermanent()) {
            return "";
        }
        return remainingBattles + " battles left";
    }
}