package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.AllianceNotificationRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Alliance;
import com.example.valorquest.model.User;
import com.example.valorquest.service.AllianceMissionService;
import com.example.valorquest.service.AllianceService;
import com.example.valorquest.service.FriendService;

import java.util.List;

public class FriendListViewModel extends ViewModel {
    private final FriendService friendService;

    private final AllianceService allianceService;
    private final MutableLiveData<List<User>> friendsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exception> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> leaderLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isInAllianceLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionCompleted = new MutableLiveData<>();

    public FriendListViewModel() {
        this.friendService = new FriendService(new UserRepository());
        this.allianceService = new AllianceService(new AllianceRepository(), new AllianceNotificationRepository(), new UserRepository(), this.friendService, new AllianceMissionService());
    }

    public LiveData<List<User>> getFriendsLiveData() {
        return friendsLiveData;
    }

    public LiveData<Exception> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getActionCompleted() {
        return actionCompleted;
    }

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

    // 🔹 New: remove friend
    public void removeFriend(User friend, Runnable onSuccess, Runnable onFailure) {
        friendService.removeFriend(friend, success -> {
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

    public LiveData<Alliance> getAllianceLiveData() {
        return allianceLiveData;
    }

    public LiveData<User> getLeaderLiveData() {
        return leaderLiveData;
    }

    public LiveData<Boolean> getIsInAllianceLiveData() {
        return isInAllianceLiveData;
    }

    public void leaveAlliance() {
        allianceService.leaveAlliance(task -> actionCompleted.postValue(task.isSuccessful()));
    }

    public void disbandAlliance() {
        allianceService.disbandAlliance(task -> actionCompleted.postValue(task.isSuccessful()));
    }

    public void checkAllianceStatus() {
        allianceService.isCurrentUserInAlliance(isInAlliance -> {
            isInAllianceLiveData.postValue(isInAlliance);
            if (isInAlliance) {
                String currentUserId = allianceService.getCurrentUserId();
                // load user to fetch allianceId
                new UserRepository().getById(currentUserId, user -> {
                    if (user != null && user.getAllianceId() != null) {
                        new AllianceRepository().getById(user.getAllianceId(), alliance -> {
                            if (alliance != null) {
                                allianceLiveData.postValue(alliance);
                                // load leader
                                new UserRepository().getById(alliance.getLeaderId(), leader -> {
                                    if (leader != null) leaderLiveData.postValue(leader);
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}
