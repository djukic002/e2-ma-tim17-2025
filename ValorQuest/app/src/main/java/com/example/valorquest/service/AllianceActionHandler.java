package com.example.valorquest.service;

import android.content.Context;
import android.util.Log;

import com.example.valorquest.data.repositories.AllianceNotificationRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Alliance;
import com.example.valorquest.model.AllianceNotification;
import com.example.valorquest.model.User;
import com.example.valorquest.model.enums.AllianceNotificationStatus;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AllianceActionHandler {

    private static final String TAG = "AllianceActionHandler";

    public static void handleAccept(Context context, String allianceId, String senderId, String notificationId) {
        UserRepository userRepository = new UserRepository();
        AllianceRepository allianceRepository = new AllianceRepository();
        AllianceNotificationRepository notificationRepository = new AllianceNotificationRepository();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1️⃣ Update the notification status
        notificationRepository.getById(notificationId, notification -> {
            if (notification != null) {
                notification.setStatus(AllianceNotificationStatus.ACCEPTED);
                notificationRepository.save(notificationId, notification, task -> Log.d(TAG, "Notification marked as ACCEPTED"));
            }
        });

        // 2️⃣ Add user to alliance members
        allianceRepository.getById(allianceId, alliance -> {
            if (alliance != null) {
                List<String> members = alliance.getMembers();
                if (!members.contains(currentUserId)) {
                    members.add(currentUserId);
                    allianceRepository.save(allianceId, alliance, t -> Log.d(TAG, "User added to alliance members"));
                }
            }
        });

        // 3️⃣ Update user’s allianceId
        userRepository.getById(currentUserId, user -> {
            if (user != null) {
                user.setAllianceId(allianceId);
                userRepository.save(currentUserId, user, t -> Log.d(TAG, "User's allianceId updated"));
            }
        });

        // 4️⃣ Notify alliance leader
        userRepository.getById(senderId, leader -> {
            if (leader != null && leader.getFcmTokens() != null && !leader.getFcmTokens().isEmpty()) {
                List<String> tokens = leader.getFcmTokens();
                // Send notification via Node server
                com.example.valorquest.service.NotificationSender.sendAllianceLeaderNotification(context, tokens, currentUserId);
            }
        });
    }

    public static void handleDecline(Context context, String allianceId, String senderId, String notificationId) {
        AllianceNotificationRepository notificationRepository = new AllianceNotificationRepository();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        notificationRepository.getById(notificationId, notification -> {
            if (notification != null) {
                notification.setStatus(AllianceNotificationStatus.REJECTED);
                notificationRepository.save(notificationId, notification, task -> Log.d(TAG, "Notification marked as DECLINED"));
            }
        });

        // Optional: notify leader if declined
    }
}
