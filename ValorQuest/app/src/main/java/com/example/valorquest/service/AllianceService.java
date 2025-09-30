package com.example.valorquest.service;

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

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // 🔹 Callback for alliance creation + invite sending
    public interface AllianceCreationCallback {
        void onSuccess(Alliance createdAlliance);
        void onError(Exception e);
    }

    /**
     * Creates a new alliance and sends invites to selected friend IDs.
     */
    public void createAlliance(String name, List<String> friendIdsToInvite, AllianceCreationCallback callback) {
        String leaderId = getCurrentUserId();
        String allianceId = UUID.randomUUID().toString();

        Alliance alliance = new Alliance(allianceId, name, leaderId);
        alliance.getMembers().add(leaderId); // leader is automatically a member

        // Save alliance
        allianceRepository.save(allianceId, alliance, task -> {
            if (!task.isSuccessful()) {
                callback.onError(new Exception("Failed to create alliance"));
                return;
            }

            // Update leader's allianceId
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

                    // Send invites
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
                            "You have been invited to join " + sender.getUsername() + "'s" + " alliance: " + alliance.getName() + "!",
                            AllianceNotificationStatus.PENDING,
                            Timestamp.now()
                    );

                    notificationRepository.save(notificationId, notification, task -> {
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
}
