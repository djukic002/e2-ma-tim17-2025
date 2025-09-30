package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.AllianceNotification;

public class AllianceNotificationRepository extends FirebaseRepository<AllianceNotification> {
    public AllianceNotificationRepository() {
        super("allianceNotifications", AllianceNotification.class);
    }
}
