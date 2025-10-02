package com.example.valorquest.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NotificationSender {

    private static final String NODE_SERVER_URL = "http://192.168.1.34:5007/send-invites";

    public static void sendAllianceLeaderNotification(List<String> tokens, String acceptedUserId, String acceptedUsername) {
        new Thread(() -> {
            try {
                URL url = new URL(NODE_SERVER_URL); // Node server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                JSONArray tokensArray = new JSONArray();
                for (String token : tokens) {
                    tokensArray.put(token);
                }
                json.put("tokens", tokensArray);
                json.put("title", "Alliance Update");
                json.put("body", acceptedUsername + " accepted your alliance invite");

                JSONObject data = new JSONObject();
                data.put("type", "ALLIANCE_MEMBER_ACCEPTED");
                data.put("userId", acceptedUserId);
                data.put("username", acceptedUsername); // optional, if you also want username in data
                json.put("data", data);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.flush(); // ✅ important
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("NotificationSender", "Leader notification response: " + responseCode);

                conn.disconnect();
            } catch (Exception e) {
                Log.e("NotificationSender", "Failed to send leader notification", e);
            }
        }).start();
    }
}
