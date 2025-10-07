package com.example.valorquest.model.dto;

import com.example.valorquest.model.AllianceMessage;
import com.google.firebase.Timestamp;

public class AllianceMessageDto {
    private String id;
    private String senderId;
    private String senderUsername;
    private int senderAvatarId;
    private String text;
    private Timestamp timestamp;
    private boolean isCurrentUser;

    public AllianceMessageDto() {}

    public AllianceMessageDto(String id, String senderId, String senderUsername, int senderAvatarId, 
                             String text, Timestamp timestamp, boolean isCurrentUser) {
        this.id = id;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderAvatarId = senderAvatarId;
        this.text = text;
        this.timestamp = timestamp;
        this.isCurrentUser = isCurrentUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public int getSenderAvatarId() {
        return senderAvatarId;
    }

    public void setSenderAvatarId(int senderAvatarId) {
        this.senderAvatarId = senderAvatarId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }
}
