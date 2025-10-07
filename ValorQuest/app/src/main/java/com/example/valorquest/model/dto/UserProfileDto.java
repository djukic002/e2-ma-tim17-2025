package com.example.valorquest.model.dto;

import java.util.List;

public class UserProfileDto {
    private String id;
    private String username;
    private int avatarId;
    private int XP = 0;
    private int level = 0;
    private int basePP = 0;
    private int coins = 0;
    private int requiredXPForNextLevel = 0;
    private String title = "";
    private int badges = 0;

    public UserProfileDto() {}

    public UserProfileDto(String id, String username, int avatarId, int XP, int level, int basePP, int coins, int requiredXPForNextLevel, String title, int badges) {
        this.id = id;
        this.username = username;
        this.avatarId = avatarId;
        this.XP = XP;
        this.level = level;
        this.basePP = basePP;
        this.coins = coins;
        this.requiredXPForNextLevel = requiredXPForNextLevel;
        this.title = title;
        this.badges = badges;
    }

    public int getBadges() {
        return badges;
    }

    public void setBadges(int badges) {
        this.badges = badges;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public int getXP() {
        return XP;
    }

    public void setXP(int XP) {
        this.XP = XP;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBasePP() {
        return basePP;
    }

    public void setBasePP(int basePP) {
        this.basePP = basePP;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getRequiredXPForNextLevel() {
        return requiredXPForNextLevel;
    }

    public void setRequiredXPForNextLevel(int requiredXPForNextLevel) {
        this.requiredXPForNextLevel = requiredXPForNextLevel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
