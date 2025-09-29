package com.example.valorquest.model;

import com.google.firebase.Timestamp;

public class AllianceMessage {
    private String senderId;
    private String senderUsername;
    private String text;
    private Timestamp timestamp;

    public AllianceMessage() {} // Required for Firestore

    public AllianceMessage(String senderId, String senderUsername, String text, Timestamp timestamp) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.timestamp = timestamp;
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
}
