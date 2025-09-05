package com.example.valorquest.model;

public class Equipment {
    private String id;
    private String name;
    private String type;
    private String description;
    private String attribute;
    private double bonus;
    private int durability;
    private double cost;
    private boolean isUpgradable;

    public Equipment() {}
    public Equipment(String id, String name, String type, String description, String attribute, double bonus, int durability, double cost, boolean isUpgradable) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.attribute = attribute;
        this.bonus = bonus;
        this.durability = durability;
        this.cost = cost;
        this.isUpgradable = isUpgradable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean isUpgradable() {
        return isUpgradable;
    }

    public void setUpgradable(boolean upgradable) {
        isUpgradable = upgradable;
    }
}
