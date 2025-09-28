package com.example.valorquest.model;

import com.google.firebase.Timestamp;

import java.time.LocalDateTime;
public class User {
    private String id;
    private String email;
    private String username;
    private int avatarId;
    private int XP = 0;
    private int level = 0;
    private int basePP = 0;
    private Timestamp previousLeveledUpAt;
    private Timestamp leveledUpAt;
    public User() {} // Required for Firestore

    public User(String id, String email, String username, int avatarId) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.avatarId = avatarId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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

    public Timestamp getLeveledUpAt() {
        return leveledUpAt;
    }

    public void setLeveledUpAt(Timestamp leveledUpAt) {
        this.leveledUpAt = leveledUpAt;
    }

    public Timestamp getPreviousLeveledUpAt() {
        return previousLeveledUpAt;
    }

    public void setPreviousLeveledUpAt(Timestamp previousLeveledUpAt) {
        this.previousLeveledUpAt = previousLeveledUpAt;
    }
}
