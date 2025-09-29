package com.example.valorquest.service;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.data.repositories.BossRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Boss;
import com.example.valorquest.model.QuestExecution;
import com.example.valorquest.model.enums.BossStatus;
import com.example.valorquest.utils.RepositoryCallback;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BossService {
    private final BossRepository bossRepository;
    private final UserRepository userRepository;
    private final QuestService questService;
    @Inject
    public BossService(QuestService questService) {
        this.bossRepository = new BossRepository();
        this.userRepository = new UserRepository();
        this.questService = questService;
    }

    public void handleBossAfterLevelUp(String userId) {
        bossRepository.getMaxLevelBossForUser(userId, maxBoss -> {
            if (maxBoss == null) {
                createNewBoss(userId, null);
                return;
            }

            if (!maxBoss.getStatus().equals(BossStatus.DEFEATED)) {
                maxBoss.setCurrentHp(maxBoss.getOriginalHp());
                maxBoss.setStatus(BossStatus.ACTIVE);
                maxBoss.setAttacksRemaining(5);

                bossRepository.save(maxBoss.getId(), maxBoss, task -> {
                    if (task.isSuccessful()) {
                        System.out.println("Boss reset for user: " + userId);
                    }
                });
            } else {
                // Defeated → new boss
                createNewBoss(userId, maxBoss);
            }

            // pozvati neki event navigaciju nesto..
        });
    }

    private void createNewBoss(String userId, Boss lastBoss) {
        calculateHitChance(userId, hitChance -> {
            Boss newBoss;
            if (lastBoss == null) {
                newBoss = new Boss(userId, "Gorlock the Destroyer", hitChance);
            } else {
                newBoss = new Boss(lastBoss, hitChance);
            }

            bossRepository.save(newBoss.getId(), newBoss, task -> {
                if (task.isSuccessful()) {
                    System.out.println("New boss created for user: " + userId + " Boss id: " + newBoss.getId());
                }
            });
        });
    }
    public void calculateHitChance(String userId, RepositoryCallback<Double> callback) {
        userRepository.getById(userId, user -> {
            if (user == null) {
                callback.onComplete(0.5);
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                int oldUserLevel = user.getLevel() - 1;

                List<QuestExecution> allCompleted = questService.getCompletedForLevelWithoutQuotaExceeding(user.getId(), oldUserLevel);
                List<QuestExecution> oldCompleted = questService.getOldCompletedForLevelWithoutQuotaExceeding(user.getId(), oldUserLevel);
                List<QuestExecution> created = questService.getCreatedExecByLevelAndStatus(user.getId(), oldUserLevel);

                double denominator = oldCompleted.size() + created.size();
                double hitChance = 0.0;

                if (denominator > 0) {
                    hitChance = (double) allCompleted.size() / denominator;
                    hitChance = Math.min(hitChance, 1.0); // cap to 1.0
                }

                callback.onComplete(hitChance);
            });
        });
    }
}
