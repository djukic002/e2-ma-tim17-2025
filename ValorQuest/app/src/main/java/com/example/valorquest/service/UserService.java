package com.example.valorquest.service;

import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Quest;
import com.example.valorquest.model.User;
import com.example.valorquest.utils.LevelSystem;
import com.example.valorquest.utils.QuestResultListener;
import com.example.valorquest.utils.QuestXPCalculator;
import com.example.valorquest.utils.QuestResultListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {
    private UserRepository userRepository;
    @Inject
    public UserService() {
        this.userRepository = new UserRepository();
    }

    public void completeQuest(Quest quest, boolean xpForDifficulty, boolean xpForImportance, QuestResultListener<Integer> onComplete) {
        userRepository.getById(quest.getUserId(), user -> {
            if (user == null) {
                onComplete.onResult(null);
                return;
            }

            Pair<Integer, Boolean> pair;

            int diffXp = 0;
            if(xpForDifficulty){
                diffXp = QuestXPCalculator.getQuestXPDifficulty(quest.getDifficulty(), user.getLevel());
            }

            int impXp = 0;
            if(xpForImportance){
                impXp = QuestXPCalculator.getQuestXPImportance(quest.getImportance(), user.getLevel());
            }

            int earnedXP = diffXp + impXp;

            pair = new Pair<>(earnedXP, addXP(user, earnedXP));
            userRepository.save(user.getId(), user, unused -> {
                onComplete.onResult(pair);
            });
        });
    }

    private boolean addXP(User user, int earnedXP) {
        user.setXP(user.getXP() + earnedXP);
        int XPForNextLevel = LevelSystem.getXPForLevel(user.getLevel() + 1);

        if (user.getXP() >= XPForNextLevel) {
            user.setXP(user.getXP() - XPForNextLevel);
            user.setLevel(user.getLevel() + 1);
            if (user.getLeveledUpAt() != null)
                user.setPreviousLeveledUpAt(user.getLeveledUpAt());
            user.setLeveledUpAt(Timestamp.now());
            rewardUser(user);
            return true;
        }

        return false;
    }

    private void rewardUser(User user) {
        user.setBasePP(user.getBasePP() + LevelSystem.getPPForLevel(user.getLevel()));
    }

    public void getUserProfile() {

    }

    public void getUserProfile(String userId, com.example.valorquest.utils.RepositoryCallback<com.example.valorquest.model.dto.UserProfileDto> callback) {
        userRepository.getById(userId, user -> {
            if (user == null) {
                callback.onComplete(null);
                return;
            }

            int requiredXPForNextLevel = com.example.valorquest.utils.LevelSystem.getXPForLevel(user.getLevel() + 1);
            String title = com.example.valorquest.utils.LevelSystem.getTitle(user.getLevel());

            com.example.valorquest.model.dto.UserProfileDto dto = new com.example.valorquest.model.dto.UserProfileDto(
                    user.getId(),
                    user.getUsername(),
                    user.getAvatarId(),
                    user.getXP(),
                    user.getLevel(),
                    user.getBasePP(),
                    user.getCoins(),
                    requiredXPForNextLevel,
                    title
            );

            callback.onComplete(dto);
        });
    }
}
