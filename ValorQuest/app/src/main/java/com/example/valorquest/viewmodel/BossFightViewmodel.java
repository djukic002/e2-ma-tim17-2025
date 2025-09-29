package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Boss;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.User;
import com.example.valorquest.service.BossService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossFightViewmodel extends ViewModel {
    private final BossService bossService;

    private final UserRepository userRepository;
    @Inject
    public BossFightViewmodel(BossService bossService) {
        this.bossService = bossService;
        this.userRepository = new UserRepository();
    }
    public LiveData<Result<String>> saveBossLiveData(Boss boss) {
        return bossService.saveBoss(boss);
    }
    public LiveData<Result<Boss>> getActiveBossForUser(String userId) {
        return bossService.getActiveBossForUserLiveData(userId);
    }
    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        userRepository.getById(userId, user -> {
            userLiveData.postValue(user);
        });
        return userLiveData;
    }
}
