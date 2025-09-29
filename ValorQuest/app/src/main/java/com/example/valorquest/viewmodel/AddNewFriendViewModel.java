package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.User;
import com.example.valorquest.service.SocialService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class AddNewFriendViewModel extends ViewModel {
    private final SocialService socialService;

    private final MutableLiveData<List<User>> _allUsers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<User>> _filteredUsers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> _error = new MutableLiveData<>(null);

    public LiveData<List<User>> allUsers = _allUsers;
    public LiveData<List<User>> filteredUsers = _filteredUsers;
    public LiveData<String> error = _error;

    private List<User> cachedUsers = new ArrayList<>();

    @Inject
    public AddNewFriendViewModel() {
        this.socialService = new SocialService(new UserRepository());
    }

    // 🔹 Load all potential new friends
    public void loadUsers() {
        socialService.getAllNonFriends(new SocialService.UsersCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                cachedUsers = users;
                _allUsers.postValue(users);
                _filteredUsers.postValue(users);
            }

            @Override
            public void onError(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }

    // 🔹 Search filter
    public void filterUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            _filteredUsers.postValue(cachedUsers);
            return;
        }

        String lower = query.toLowerCase();
        List<User> filtered = cachedUsers.stream()
                .filter(u -> u.getUsername() != null && u.getUsername().toLowerCase().contains(lower))
                .collect(Collectors.toList());

        _filteredUsers.postValue(filtered);
    }

    public void addFriend(User friend, Runnable onSuccess, Runnable onFailure) {
        socialService.addFriend(friend, success -> {
            if (success) {
                // Remove from cache so they don’t show up again
                cachedUsers.removeIf(u -> u.getId().equals(friend.getId()));
                _allUsers.postValue(cachedUsers);
                _filteredUsers.postValue(cachedUsers);

                onSuccess.run();
            } else {
                onFailure.run();
            }
        });
    }
}
