package com.example.valorquest.service;

import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.User;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SocialService {
    private final UserRepository userRepository;

    @Inject
    public SocialService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public interface FriendsCallback {
        void onFriendsLoaded(List<User> friends);
        void onError(Exception e);
    }

    public interface UsersCallback {
        void onUsersLoaded(List<User> users);
        void onError(Exception e);
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // 🔹 Get current user's friends
    public void getUserFriends(String userId, FriendsCallback callback) {
        userRepository.getById(userId, currentUser -> {
            if (currentUser == null) {
                callback.onError(new Exception("User not found"));
                return;
            }

            List<String> friendIds = currentUser.getFriends();
            if (friendIds == null || friendIds.isEmpty()) {
                callback.onFriendsLoaded(new ArrayList<>());
                return;
            }

            List<User> friends = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(0);

            for (String fid : friendIds) {
                userRepository.getById(fid, friend -> {
                    if (friend != null) {
                        friends.add(friend);
                    }
                    if (counter.incrementAndGet() == friendIds.size()) {
                        callback.onFriendsLoaded(friends);
                    }
                });
            }
        });
    }

    // 🔹 Get all non-friends for "Add New Friend"
    public void getAllNonFriends(UsersCallback callback) {
        String currentUserId = getCurrentUserId();

        getUserFriends(currentUserId, new FriendsCallback() {
            @Override
            public void onFriendsLoaded(List<User> friends) {
                List<String> friendIds = friends.stream().map(User::getId).collect(Collectors.toList());

                userRepository.getAll(allUsers -> {
                    if (allUsers == null) {
                        callback.onError(new Exception("Failed to load users"));
                        return;
                    }

                    List<User> result = allUsers.stream()
                            .filter(u -> !u.getId().equals(currentUserId)) // exclude self
                            .filter(u -> !friendIds.contains(u.getId()))   // exclude friends
                            .collect(Collectors.toList());

                    callback.onUsersLoaded(result);
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // 🔹 Add friend
    public void addFriend(User friend, RepositoryCallback<Boolean> callback) {
        String currentUserId = getCurrentUserId();

        userRepository.getById(currentUserId, currentUser -> {
            if (currentUser == null) {
                callback.onComplete(false);
                return;
            }

            if (!currentUser.getFriends().contains(friend.getId())) {
                currentUser.getFriends().add(friend.getId());
            }

            userRepository.save(currentUserId, currentUser, task -> {
                callback.onComplete(task.isSuccessful());
            });
        });
    }

    // 🔹 Remove friend
    public void removeFriend(User friend, RepositoryCallback<Boolean> callback) {
        String currentUserId = getCurrentUserId();

        userRepository.getById(currentUserId, currentUser -> {
            if (currentUser == null) {
                callback.onComplete(false);
                return;
            }

            currentUser.getFriends().remove(friend.getId());

            userRepository.save(currentUserId, currentUser, task -> {
                callback.onComplete(task.isSuccessful());
            });
        });
    }
}
