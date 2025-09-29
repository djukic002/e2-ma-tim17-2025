package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.CategoryRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Boss;
import com.example.valorquest.model.Result;
import com.example.valorquest.service.BossService;
import com.example.valorquest.service.QuestService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossFightViewmodel extends ViewModel {
    private final BossService bossService;
    @Inject
    public BossFightViewmodel(BossService bossService) {
        this.bossService = bossService;
    }
    public LiveData<Result<String>> saveBossLiveData(Boss boss) {
        return bossService.saveBoss(boss);
    }
    public LiveData<Result<Boss>> getActiveBossForUser(String userId) {
        return bossService.getActiveBossForUserLiveData(userId);
    }
}
