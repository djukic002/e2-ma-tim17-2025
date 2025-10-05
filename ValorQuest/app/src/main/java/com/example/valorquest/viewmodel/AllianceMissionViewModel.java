package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.CategoryRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.AllianceMission;
import com.example.valorquest.model.User;
import com.example.valorquest.model.dto.MissionSummaryDto;
import com.example.valorquest.service.AllianceMissionService;
import com.example.valorquest.service.AllianceService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AllianceMissionViewModel extends ViewModel {
    private final AllianceMissionService missionService;
    private final UserRepository userRepository;
    @Inject
    public AllianceMissionViewModel(AllianceMissionService missionService) {
        this.missionService = missionService;
        userRepository = new UserRepository();
    }

    public LiveData<AllianceMission> createMission() {
        MutableLiveData<AllianceMission> result = new MutableLiveData<>();
        missionService.createMission(result::postValue);
        return result;
    }

    public LiveData<MissionSummaryDto> getActiveMissionSummary() {
        MutableLiveData<MissionSummaryDto> result = new MutableLiveData<>();
        missionService.getActiveMissionSummary(result::postValue);
        return result;
    }
    public LiveData<Boolean> isCurrentUserLeader() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        String userId = missionService.getCurrentUserId();

        missionService.isUserLeader(userId, isLeader -> {
            result.postValue(isLeader);
        });

        return result;
    }

    public LiveData<User> getCurrentUser() {
        MutableLiveData<User> result = new MutableLiveData<>();

        String currentUserId = missionService.getCurrentUserId();
        userRepository.getById(currentUserId, user -> {
            result.postValue(user);
        });

        return result;
    }
}
