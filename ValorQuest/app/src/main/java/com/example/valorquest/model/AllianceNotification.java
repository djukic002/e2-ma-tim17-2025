package com.example.valorquest.model;

import com.example.valorquest.model.enums.AllianceNotificationStatus;
import com.google.firebase.Timestamp;

public class AllianceNotification {
    private String id;
    private String senderId;
    private String receiverId;
    private String allianceId;
    private String message;
    private AllianceNotificationStatus status;
    private Timestamp createdAt;

    public AllianceNotification() {}

    public AllianceNotification(String id, String senderId, String receiverId,
                                String allianceId, String message,
                                AllianceNotificationStatus status, Timestamp createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.allianceId = allianceId;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AllianceNotificationStatus getStatus() {
        return status;
    }

    public void setStatus(AllianceNotificationStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
