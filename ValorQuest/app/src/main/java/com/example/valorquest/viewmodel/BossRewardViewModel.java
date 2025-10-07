package com.example.valorquest.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.BossRepository;
import com.example.valorquest.data.repositories.EquipmentRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Equipment;
import com.example.valorquest.service.AllianceMissionService;
import com.example.valorquest.service.BossService;
import com.example.valorquest.service.EquipmentService;
import com.example.valorquest.utils.RepositoryCallback;

import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossRewardViewModel extends ViewModel {
    private final UserRepository userRepository;

    private final EquipmentService equipmentService;
    @Inject
    public BossRewardViewModel() {
        userRepository = new UserRepository();
        equipmentService = new EquipmentService(new EquipmentRepository(), userRepository, new BossRepository());
    }
    public void rewardUserWithGold(String userId, int gold, RepositoryCallback<Boolean> callback) {
        userRepository.getById(userId, user -> {
            if (user == null) {
                Log.e("BossRewardViewModel", "User not found: " + userId);
                callback.onComplete(false);
                return;
            }

            user.setCoins(user.getCoins() + gold);

            userRepository.save(user.getId(), user, saveTask -> {
                if (saveTask.isSuccessful()) {
                    Log.i("BossRewardViewModel", "Successfully rewarded " + gold + " coins to user " + userId);
                    callback.onComplete(true);
                } else {
                    Log.e("BossRewardViewModel", "Failed to reward user " + userId, saveTask.getException());
                    callback.onComplete(false);
                }
            });
        });
    }
    public void giveRandomBossReward(String userId, RepositoryCallback<Equipment> callback) {
        Random random = new Random();
        String type = (random.nextInt(100) < 95) ? "armor" : "weapon";

        equipmentService.giveRandomEquipment(userId, type, eq -> {
            if (eq != null) {
                Log.i("BossRewardViewModel",
                        "User " + userId + " received " + type + ": " + eq.toString());
                callback.onComplete(eq);
            } else {
                Log.w("BossRewardViewModel",
                        "Failed to generate " + type + " for user " + userId);
                callback.onComplete(null);
            }
        });
    }
}
