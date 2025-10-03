package com.example.valorquest.viewmodel;

import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.AllianceNotificationRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Alliance;
import com.example.valorquest.model.User;
import com.example.valorquest.service.AllianceService;
import com.example.valorquest.service.FriendService;

import java.util.List;

public class AllianceMembersViewModel extends ViewModel {
    private final FriendService friendService;
    private final AllianceService allianceService;
    private final UserRepository userRepository;

    private final MutableLiveData<List<User>> allianceMembersLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exception> errorLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<User>> potentialMembersLiveData = new MutableLiveData<>();

    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();

    public LiveData<List<User>> getAllianceMembersLiveData() { return allianceMembersLiveData; }
    public LiveData<List<User>> getPotentialMembersLiveData() { return potentialMembersLiveData; }
    public LiveData<Exception> getErrorLiveData() { return errorLiveData; }

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    public LiveData<Alliance> getAllianceLiveData() {
        return allianceLiveData;
    }

    public AllianceMembersViewModel() {
        this.friendService = new FriendService(new UserRepository());
        this.allianceService = new AllianceService(
                new AllianceRepository(),
                new AllianceNotificationRepository(),
                new UserRepository(),
                this.friendService
        );
        this.userRepository = new UserRepository();
    }

    public void loadAlliance() {
        String currentUserId = allianceService.getCurrentUserId();
        // load user to fetch allianceId
        new UserRepository().getById(currentUserId, user -> {
            if (user != null && user.getAllianceId() != null) {
                new AllianceRepository().getById(user.getAllianceId(), alliance -> {
                    if (alliance != null) {
                        allianceLiveData.postValue(alliance);
                        loadAllianceMembers(alliance);
                        loadPotentialMembers();
                    }
                });
            }
        });
    }

    private void loadAllianceMembers(Alliance alliance) {
        allianceService.getAllianceMembers(alliance.getId(), new AllianceService.AllianceUsersCallback() {
            @Override
            public void onUsersLoaded(List<User> members) {
                allianceMembersLiveData.postValue(members);
            }

            @Override
            public void onError(Exception e) {
                errorLiveData.postValue(e);
            }
        });
    }

    private void loadPotentialMembers() {
        allianceService.getPotentialMembers(new AllianceService.AllianceUsersCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                potentialMembersLiveData.postValue(users);
            }

            @Override
            public void onError(Exception e) {
                errorLiveData.postValue(e);
            }
        });
    }

    public void inviteNewMembers(List<String> inviteIds) {
        String currentUserId = allianceService.getCurrentUserId();
        userRepository.getById(currentUserId, user -> {
            allianceService.sendAllianceInvites(user, allianceLiveData.getValue(), inviteIds, result -> {
                if (result)
                    _toastMessage.setValue("Invites sent.");
                else
                    _toastMessage.setValue("Failed to send some invites");
            });
        });
    }
}
