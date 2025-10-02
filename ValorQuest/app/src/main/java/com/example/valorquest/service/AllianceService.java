package com.example.valorquest.service;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AllianceService {
    private final AllianceRepository allianceRepository;
    private final AllianceNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;

    @Inject
    public AllianceService(AllianceRepository allianceRepository,
                           AllianceNotificationRepository notificationRepository,
                           UserRepository userRepository,
                           FriendService friendService) {
        this.allianceRepository = allianceRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.friendService = friendService;
    }

    public String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public interface AllianceCreationCallback {
        void onSuccess(Alliance createdAlliance);
        void onError(Exception e);
    }

    public void createAlliance(String name, List<String> friendIdsToInvite, AllianceCreationCallback callback) {
        String leaderId = getCurrentUserId();
        String allianceId = UUID.randomUUID().toString();

        Alliance alliance = new Alliance(allianceId, name, leaderId);
        alliance.getMembers().add(leaderId); // leader is automatically a member

        allianceRepository.save(allianceId, alliance, task -> {
            if (!task.isSuccessful()) {
                callback.onError(new Exception("Failed to create alliance"));
                return;
            }

            userRepository.getById(leaderId, leader -> {
                if (leader == null) {
                    callback.onError(new Exception("Leader not found"));
                    return;
                }

                leader.setAllianceId(allianceId);
                userRepository.save(leaderId, leader, t -> {
                    if (!t.isSuccessful()) {
                        callback.onError(new Exception("Failed to update leader"));
                        return;
                    }

                    sendAllianceInvites(leader, alliance, friendIdsToInvite, new RepositoryCallback<Boolean>() {
                        @Override
                        public void onComplete(Boolean result) {
                            if (result) {
                                callback.onSuccess(alliance);
                            } else {
                                callback.onError(new Exception("Failed to send some invites"));
                            }
                        }
                    });
                });
            });
        });
    }

    public void acceptInvite(String allianceId, String notificationId, String senderId) {
        String currentUserId = getCurrentUserId();

        // 1️⃣ Update the notification status
        notificationRepository.getById(notificationId, notification -> {
            if (notification != null) {
                notification.setStatus(AllianceNotificationStatus.ACCEPTED);
                notificationRepository.save(notificationId, notification, task -> Log.d("", "Notification marked as ACCEPTED"));
            }
        });

        // 2️⃣ Add user to alliance members
        allianceRepository.getById(allianceId, alliance -> {
            if (alliance != null) {
                List<String> members = alliance.getMembers();
                if (!members.contains(currentUserId)) {
                    members.add(currentUserId);
                    allianceRepository.save(allianceId, alliance, t -> Log.d("AllianceInviteAccept", "User added to alliance members"));
                }
            }
        });

        // 3️⃣ Update user’s allianceId
        userRepository.getById(currentUserId, user -> {
            if (user != null) {
                user.setAllianceId(allianceId);
                userRepository.save(currentUserId, user, t -> Log.d("AllianceInviteAccept", "User's allianceId updated"));
            }
        });

        userRepository.getById(currentUserId, acceptedUser -> {
            if (acceptedUser != null) {
                String acceptedUsername = acceptedUser.getUsername();

                userRepository.getById(senderId, leader -> {
                    if (leader != null && leader.getFcmTokens() != null && !leader.getFcmTokens().isEmpty()) {
                        List<String> tokens = leader.getFcmTokens();
                        // Send notification via Node server with username
                        com.example.valorquest.utils.NotificationSender.sendAllianceLeaderNotification(
                                tokens,
                                acceptedUser.getId(),
                                acceptedUsername
                        );
                    }
                });
            }
        });
    }

    public void isCurrentUserInAlliance(BooleanCheckCallback callback) {
        String currentUserId = getCurrentUserId();
        userRepository.getById(currentUserId, user -> {
            boolean result = user.getAllianceId() != null;
            Log.d("DEBUG", "U ALIJANSI SERVIS " + result + " " + user.getUsername());
            callback.onResult(result);
        });
    }

    public void isUserLeader(String userId, BooleanCheckCallback callback) {
        userRepository.getById(userId, user -> {
            allianceRepository.getById(user.getAllianceId(), alliance -> {
                Log.d("KURCINELA", alliance.getLeaderId() + " - " + userId);
                boolean result = Objects.equals(alliance.getLeaderId(), userId);
                callback.onResult(result);
            });
        });

    }

    public interface BooleanCheckCallback {
        void onResult(boolean result);
    }

    private void sendAllianceInvites(User sender, Alliance alliance, List<String> friendIdsToInvite, RepositoryCallback<Boolean> callback) {
        friendService.getUserFriends(sender.getId(), new FriendService.FriendsCallback() {
            @Override
            public void onFriendsLoaded(List<User> friends) {
                List<String> validFriendIds = new ArrayList<>();
                for (User f : friends) {
                    if (friendIdsToInvite.contains(f.getId())) {
                        validFriendIds.add(f.getId());
                    }
                }

                if (validFriendIds.isEmpty()) {
                    callback.onComplete(true);
                    return;
                }

                AtomicInteger counter = new AtomicInteger(0);
                for (String fid : validFriendIds) {
                    String notificationId = UUID.randomUUID().toString();
                    AllianceNotification notification = new AllianceNotification(
                            notificationId,
                            sender.getId(),
                            fid,
                            alliance.getId(),
                            "You have been invited to join " + sender.getUsername() + "'s alliance: " + alliance.getName() + "!",
                            AllianceNotificationStatus.PENDING,
                            Timestamp.now()
                    );

                    notificationRepository.save(notificationId, notification, task -> {
                        if (task.isSuccessful()) {
                            // ✅ Send FCM notification via Node.js
                            User receiver = friends.stream().filter(u -> u.getId().equals(fid)).findFirst().orElse(null);
                            if (receiver != null && !receiver.getFcmTokens().isEmpty()) {
                                sendFCMInvite(notification, receiver.getFcmTokens());
                            }
                        }

                        if (counter.incrementAndGet() == validFriendIds.size()) {
                            callback.onComplete(true);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onComplete(false);
            }
        });
    }

    private void sendFCMInvite(AllianceNotification notification, List<String> receiverTokens) {
        if (receiverTokens == null || receiverTokens.isEmpty()) return;

        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.34:5007/send-invites"); // Node server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                JSONArray tokensArray = new JSONArray();
                for (String token : receiverTokens) {
                    tokensArray.put(token);
                }
                json.put("tokens", tokensArray);
                json.put("title", "A new fellowship awaits!");
                json.put("body", notification.getMessage());

                JSONObject data = new JSONObject();
                data.put("type", "ALLIANCE_INVITE");
                data.put("allianceId", notification.getAllianceId());
                data.put("senderId", notification.getSenderId());
                data.put("notificationId", notification.getId());
                json.put("data", data);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();  // <-- flush is important!
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("FCM", "Response code: " + responseCode);

                conn.disconnect();
            } catch (Exception e) {
                Log.e("FCM", "Error sending FCM invite", e);
            }
        }).start();
    }



}
