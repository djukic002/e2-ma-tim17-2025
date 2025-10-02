package com.example.valorquest.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.valorquest.R;
import com.example.valorquest.ui.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "default_channel";
    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null && !remoteMessage.getData().isEmpty()) {
            String type = remoteMessage.getData().get("type");
            if ("ALLIANCE_INVITE".equals(type)) {
                String title = remoteMessage.getData().get("title");
                String body = remoteMessage.getData().get("body");
                String allianceId = remoteMessage.getData().get("allianceId");
                String senderId = remoteMessage.getData().get("senderId");
                String notificationId = remoteMessage.getData().get("notificationId");

                showAllianceInviteNotification(title, body, allianceId, senderId, notificationId);
            } else if ("ALLIANCE_MEMBER_ACCEPTED".equals(type)) {
                String title = remoteMessage.getData().get("title");
                String body = remoteMessage.getData().get("body");
                String userId = remoteMessage.getData().get("userId");

                showAllianceMemberAcceptedNotification(title, body, userId);
            }
        }
    }

    private void showAllianceMemberAcceptedNotification(String title, String body, String userId) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alliance_updates",
                    "Alliance Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "alliance_updates")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // dismissible this time, since it's info-only

        manager.notify(new Random().nextInt(), builder.build());
        Log.d("LEADER NOTIFICATION", "Notification shown");
    }

    private void showAllianceInviteNotification(String title, String body, String allianceId, String senderId, String notificationId) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alliance_channel",
                    "Alliance Invites",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Intent acceptIntent = new Intent(this, MainActivity.class);
        acceptIntent.setAction("ACTION_ACCEPT_INVITE");
        acceptIntent.putExtra("allianceId", allianceId);
        acceptIntent.putExtra("senderId", senderId);
        acceptIntent.putExtra("notificationId", notificationId);
        PendingIntent acceptPending = PendingIntent.getActivity(this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent declineIntent = new Intent(this, AllianceInviteReceiver.class);
        declineIntent.setAction("ACTION_DECLINE_INVITE");
        declineIntent.putExtra("allianceId", allianceId);
        declineIntent.putExtra("senderId", senderId);
        declineIntent.putExtra("notificationId", notificationId);
        PendingIntent declinePending = PendingIntent.getBroadcast(this, 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "alliance_channel")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true) // persistent until a button is tapped
                .addAction(android.R.drawable.ic_input_add, "Accept", acceptPending)
                .addAction(android.R.drawable.ic_delete, "Decline", declinePending);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        manager.notify(notificationId.hashCode(), notification);
    }
}
