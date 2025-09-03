package com.example.valorquest.model;

public class User {
    private String id;
    private String email;
    private String username;
    private int avatarId;

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
}
