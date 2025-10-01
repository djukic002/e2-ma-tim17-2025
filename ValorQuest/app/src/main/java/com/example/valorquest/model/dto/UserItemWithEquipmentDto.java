package com.example.valorquest.model.dto;

import com.example.valorquest.model.Equipment;
import com.example.valorquest.model.UserItem;

public class UserItemWithEquipmentDto {
    private final String userItemId;
    private final String equipmentId;
    private final String name;
    private final String type;
    private final String description;
    private final String attribute;
    private final double bonus;
    private final int durability;
    private final int remainingBattles;
    private final int reforgeLevel;
    private final int upgradeLevel;
    private final boolean isUpgradable;

    public UserItemWithEquipmentDto(UserItem userItem, Equipment equipment) {
        this.userItemId = userItem.getId();
        this.equipmentId = equipment.getId();
        this.name = equipment.getName();
        this.type = equipment.getType();
        this.description = equipment.getDescription();
        this.attribute = equipment.getAttribute();
        this.bonus = equipment.getBonus();
        this.durability = equipment.getDurability();
        this.remainingBattles = userItem.getRemainingBattles();
        this.reforgeLevel = userItem.getReforgeLevel();
        this.upgradeLevel = userItem.getUpgradeLevel();
        this.isUpgradable = equipment.isUpgradable();
    }

    // Getters

    public String getUserItemId() { return userItemId; }
    public String getEquipmentId() { return equipmentId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getAttribute() { return attribute; }
    public double getBonus() { return bonus; }
    public int getDurability() { return durability; }
    public int getRemainingBattles() { return remainingBattles; }
    public int getReforgeLevel() { return reforgeLevel; }
    public int getUpgradeLevel() { return upgradeLevel; }
    public boolean isUpgradable() { return isUpgradable; }
}