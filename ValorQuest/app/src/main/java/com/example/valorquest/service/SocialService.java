package com.example.valorquest.service;

import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void getUserFriends(String userId, FriendsCallback callback) {
        userRepository.getById(userId, currentUser -> {
            if (currentUser == null) {
                callback.onError(new Exception("User not found"));
                return;
            }

            List<String> friendIds = currentUser.getFriends();
            if (friendIds.isEmpty()) {
                callback.onFriendsLoaded(new ArrayList<>());
                return;
            }

            List<User> friends = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(0);

            // Step 2: Fetch each friend by ID
            for (String fid : friendIds) {
                userRepository.getById(fid, friend -> {
                    if (friend != null) {
                        friends.add(friend);
                    }
                    if (counter.incrementAndGet() == friendIds.size()) {
                        // All friends fetched
                        callback.onFriendsLoaded(friends);
                    }
                });
            }
        });
    }
}
