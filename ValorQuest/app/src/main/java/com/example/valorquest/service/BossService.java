package com.example.valorquest.service;

import com.example.valorquest.data.repositories.BossRepository;
import com.example.valorquest.model.Boss;
import com.example.valorquest.model.enums.BossStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BossService {
    private final BossRepository bossRepository;

    @Inject
    public BossService() {
        this.bossRepository = new BossRepository();
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
        Boss newBoss;
        double hitChance = 60.0;

        if(lastBoss == null){
            newBoss = new Boss(userId, "Gorlock the Destroyer", hitChance);
        }else{
            newBoss = new Boss(lastBoss, hitChance);
        }

        bossRepository.save(newBoss.getId(), newBoss, task -> {
            if (task.isSuccessful()) {
                System.out.println("New boss created for user: " + userId + " Boss id: " + newBoss.getId());
            }
        });
    }
}
