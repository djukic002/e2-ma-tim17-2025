package com.example.valorquest.service;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class NotificationSender {

    private static final String NODE_SERVER_URL = "http://192.168.1.34:5007/send-invites";

    public static void sendAllianceLeaderNotification(Context context, List<String> tokens, String acceptedUserId) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("tokens", new JSONArray(tokens));
                body.put("title", "Alliance Update");
                body.put("body", "User " + acceptedUserId + " accepted your alliance invite");
                body.put("data", new JSONObject().put("type", "ALLIANCE_MEMBER_ACCEPTED").put("userId", acceptedUserId));

                URL url = new URL(NODE_SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("utf-8"));
                }

                int responseCode = conn.getResponseCode();
                Log.d("NotificationSender", "Response code: " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                Log.e("NotificationSender", "Failed to send notification", e);
            }
        }).start();
    }
}
