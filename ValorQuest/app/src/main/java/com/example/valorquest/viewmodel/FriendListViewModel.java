package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.User;
import com.example.valorquest.service.SocialService;

import java.util.List;

public class FriendListViewModel extends ViewModel {
    private final SocialService socialService;
    private final MutableLiveData<List<User>> friendsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exception> errorLiveData = new MutableLiveData<>();

    public FriendListViewModel() {
        this.socialService = new SocialService(new UserRepository());
    }

    public LiveData<List<User>> getFriendsLiveData() {
        return friendsLiveData;
    }

    public LiveData<Exception> getErrorLiveData() {
        return errorLiveData;
    }

    public void loadFriends(String currentUserId) {
        socialService.getUserFriends(currentUserId, new SocialService.FriendsCallback() {
            @Override
            public void onFriendsLoaded(List<User> friends) {
                friendsLiveData.postValue(friends);
            }

            @Override
            public void onError(Exception e) {
                errorLiveData.postValue(e);
            }
        });
    }

    // 🔹 New: remove friend
    public void removeFriend(User friend, Runnable onSuccess, Runnable onFailure) {
        socialService.removeFriend(friend, success -> {
            if (success) {
                // update list after removal
                List<User> current = friendsLiveData.getValue();
                if (current != null) {
                    current.remove(friend);
                    friendsLiveData.postValue(current);
                }
                onSuccess.run();
            } else {
                onFailure.run();
            }
        });
    }
}
