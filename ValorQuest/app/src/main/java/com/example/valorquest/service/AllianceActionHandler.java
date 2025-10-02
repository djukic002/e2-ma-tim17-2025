package com.example.valorquest.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.valorquest.data.repositories.AllianceNotificationRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Alliance;
import com.example.valorquest.model.AllianceNotification;
import com.example.valorquest.model.User;
import com.example.valorquest.model.enums.AllianceNotificationStatus;
import com.example.valorquest.ui.MainActivity;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AllianceActionHandler {

    private static final String TAG = "AllianceActionHandler";

    public static void handleAccept(Context context, String allianceId, String senderId, String notificationId) {
//        AllianceService allianceService = new AllianceService(new AllianceRepository(), new AllianceNotificationRepository(), new UserRepository(), new FriendService(new UserRepository()));
//        Log.d("DEBUG", "RADI BILO STA ACTION HANDLER");
//        allianceService.isCurrentUserInAlliance(isInAlliance -> {
//            if (isInAlliance) {
//                Log.d("DEBUG", "U ALIJANSI ACTION HANDLER");
//                Intent intent = new Intent(context, MainActivity.class);
//                intent.putExtra("allianceId", allianceId);
//                intent.putExtra("senderId", senderId);
//                intent.putExtra("notificationId", notificationId);
//                intent.putExtra("action", "accept_invite");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                context.startActivity(intent);
//            } else {
//                allianceService.acceptInvite(allianceId, notificationId, senderId);
//            }
//        });
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
