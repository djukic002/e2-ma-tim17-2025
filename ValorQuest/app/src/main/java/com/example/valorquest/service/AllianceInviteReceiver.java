package com.example.valorquest.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.valorquest.service.AllianceActionHandler;

public class AllianceInviteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String allianceId = intent.getStringExtra("allianceId");
        String senderId = intent.getStringExtra("senderId");
        String notificationId = intent.getStringExtra("notificationId");

        if ("ACTION_ACCEPT_INVITE".equals(action)) {
            AllianceActionHandler.handleAccept(context, allianceId, senderId, notificationId);
        } else if ("ACTION_DECLINE_INVITE".equals(action)) {
            AllianceActionHandler.handleDecline(context, allianceId, senderId, notificationId);
        }

        // Remove notification after user taps
        NotificationManagerCompat.from(context).cancel(notificationId.hashCode());
    }
}
