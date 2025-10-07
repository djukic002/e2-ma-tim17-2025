package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.AllianceNotificationRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.User;
import com.example.valorquest.service.AllianceMissionService;
import com.example.valorquest.service.AllianceService;
import com.example.valorquest.service.FriendService;

import java.util.ArrayList;
import java.util.List;

public class AllianceCreationViewModel extends ViewModel {
    private final FriendService friendService;
    private final AllianceService allianceService;

    private final MutableLiveData<List<User>> friendsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exception> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> allianceCreatedLiveData = new MutableLiveData<>();

    public AllianceCreationViewModel() {
        // Keep same instantiation pattern as your other viewmodels/services
        this.friendService = new FriendService(new UserRepository());
        this.allianceService = new AllianceService(
                new AllianceRepository(),
                new AllianceNotificationRepository(),
                new UserRepository(),
                this.friendService,
                new AllianceMissionService()
        );
    }

    public LiveData<List<User>> getFriendsLiveData() { return friendsLiveData; }
    public LiveData<Exception> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getAllianceCreatedLiveData() { return allianceCreatedLiveData; }

    public void loadFriends(String currentUserId) {
        friendService.getUserFriends(currentUserId, new FriendService.FriendsCallback() {
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

    /**
     * Create alliance. invitedFriendIds is a list of IDs (strings) of friends to invite.
     */
    public void createAlliance(String name, List<String> invitedFriendIds) {
        allianceService.createAlliance(name, invitedFriendIds, new AllianceService.AllianceCreationCallback() {
            @Override
            public void onSuccess(com.example.valorquest.model.Alliance createdAlliance) {
                allianceCreatedLiveData.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                errorLiveData.postValue(e);
            }
        });
    }
}
