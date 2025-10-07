package com.example.valorquest.service;

import android.util.Log;

import com.example.valorquest.data.repositories.BossRepository;
import com.example.valorquest.data.repositories.EquipmentRepository;
import com.example.valorquest.data.repositories.UserItemRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Boss;
import com.example.valorquest.model.Equipment;
import com.example.valorquest.model.User;
import com.example.valorquest.model.UserItem;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final BossRepository bossRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, UserRepository userRepository, BossRepository bossRepository) {
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
        this.bossRepository = bossRepository;
    }
    public void buyEquipment(String equipmentId, RepositoryCallback<Boolean> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onComplete(false);
            return;
        }

        // Get user to check level and coins
        userRepository.getById(userId, user -> {
            if (user == null) {
                callback.onComplete(false);
                return;
            }

            // Check if user is level 0 (can't buy anything)
            if (user.getLevel() == 0) {
                callback.onComplete(false);
                return;
            }

            // Get equipment details
            equipmentRepository.getById(equipmentId, equipment -> {
                if (equipment == null) {
                    callback.onComplete(false);
                    return;
                }

                // Check if equipment is purchasable (potion or armor, not weapon)
                if (!"potion".equals(equipment.getType()) && !"armor".equals(equipment.getType())) {
                    callback.onComplete(false);
                    return;
                }

                // Get previous level boss to calculate actual price
                bossRepository.getMaxLevelBossForUser(userId, boss -> {
                    if (boss == null) {
                        callback.onComplete(false);
                        return;
                    }

                    // Calculate actual price: equipment.cost * boss.goldReward
                    int actualPrice = (int) (equipment.getCost() * boss.getGoldReward());

                    // Check if user has enough coins
                    if (user.getCoins() < actualPrice) {
                        callback.onComplete(false);
                        return;
                    }

                    // Deduct coins and create UserItem
                    user.setCoins(user.getCoins() - actualPrice);
                    userRepository.save(userId, user, saveTask -> {
                        if (!saveTask.isSuccessful()) {
                            callback.onComplete(false);
                            return;
                        }

                        // Create UserItem
                        UserItem userItem = new UserItem();
                        userItem.setId(java.util.UUID.randomUUID().toString());
                        userItem.setEquipmentId(equipmentId);
                        userItem.setActivated(false);
                        userItem.setRemainingBattles(equipment.getDurability());
                        userItem.setReforgeLevel(0);
                        userItem.setUpgradeLevel(0);

                        // Save UserItem
                        UserItemRepository userItemRepo = new UserItemRepository(userId);
                        userItemRepo.save(userItem.getId(), userItem, itemTask -> {
                            callback.onComplete(itemTask.isSuccessful());
                        });
                    });
                });
            });
        });
    }





    public void getActualPrice(String equipmentId, RepositoryCallback<Integer> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onComplete(0);
            return;
        }

        userRepository.getById(userId, user -> {
            if (user == null || user.getLevel() == 0) {
                callback.onComplete(0);
                return;
            }

            equipmentRepository.getById(equipmentId, equipment -> {
                if (equipment == null) {
                    callback.onComplete(0);
                    return;
                }

                bossRepository.getMaxLevelBossForUser(userId, boss -> {
                    if (boss == null) {
                        callback.onComplete(0);
                        return;
                    }

                    int actualPrice = (int) (equipment.getCost() * boss.getGoldReward());
                    callback.onComplete(actualPrice);
                });
            });
        });
    }

    public void getActiveEquipment(RepositoryCallback<List<UserItem>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onComplete(null);
            return;
        }

        UserItemRepository userItemRepo = new UserItemRepository(userId);
        userItemRepo.getAll(userItems -> {
            if (userItems == null) {
                callback.onComplete(null);
                return;
            }

            // Filter for active items only
            List<UserItem> activeItems = new ArrayList<>();
            for (UserItem userItem : userItems) {
                if (userItem.isActivated()) {
                    activeItems.add(userItem);
                }
            }

            callback.onComplete(activeItems);
        });
    }

    public void damageEquipment() {
        UserItemRepository userItemRepository = new UserItemRepository(FirebaseAuth.getInstance().getCurrentUser().getUid());
        getActiveEquipment(items -> {
            for (UserItem item : items) {
                int remainingBattles = item.getRemainingBattles();
                remainingBattles--;
                if (remainingBattles == 0) {
                    userItemRepository.delete(item.getId(), deleteTask -> {
                        if (deleteTask.isSuccessful())
                            Log.d("EQUIPMENT", "DELETED");
                        else
                            Log.d("EQUIPMENT", "BELAJ");
                    });
                } else {
                    item.setRemainingBattles(remainingBattles);
                    userItemRepository.save(item.getId(), item, task -> {
                        if (task.isSuccessful())
                            Log.d("EQUIPMENT", "UPDATED");
                        else
                            Log.d("EQUIPMENT", "BELAJ");
                    });
                }
            }
        });
    }

    public void giveRandomEquipment(String userId, String equipmentType, RepositoryCallback<Equipment> callback) {
        // Get all equipment from the repository
        UserItemRepository userItemRepo = new UserItemRepository(userId);

        equipmentRepository.getAll(equipmentList -> {
            if (equipmentList == null || equipmentList.isEmpty()) {
                callback.onComplete(null);
                return;
            }

            // Filter equipment based on type
            List<Equipment> filteredEquipment = new ArrayList<>();
            for (Equipment equipment : equipmentList) {
                if (equipmentType.equals(equipment.getType())) {
                    filteredEquipment.add(equipment);
                }
            }

            if (filteredEquipment.isEmpty()) {
                callback.onComplete(null);
                return;
            }

            // Select random equipment
            Random random = new Random();
            Equipment randomEquipment = filteredEquipment.get(random.nextInt(filteredEquipment.size()));


            userItemRepo.getAllItems(items -> {
                UserItem userItem;

                if (equipmentType.equals("weapon")){
                    for (UserItem ui : items) {
                        if (ui.getEquipmentId().equals(randomEquipment.getId())) {
                            userItem = ui;
                            int reforgeLevel = ui.getReforgeLevel();
                            userItem.setReforgeLevel(++reforgeLevel);
                            userItemRepo.save(userItem.getId(), userItem, saveTask -> {
                                if (saveTask.isSuccessful())
                                    callback.onComplete(randomEquipment);
                                else
                                    callback.onComplete(null);

                            });
                        }
                    }
                }

                userItem = new UserItem();
                userItem.setId(java.util.UUID.randomUUID().toString());
                userItem.setEquipmentId(randomEquipment.getId());
                userItem.setActivated(false);
                userItem.setRemainingBattles(randomEquipment.getDurability());
                userItem.setUpgradeLevel(0);
                userItem.setReforgeLevel(0);

                userItemRepo.save(userItem.getId(), userItem, saveTask -> {
                    if (saveTask.isSuccessful())
                        callback.onComplete(randomEquipment);
                    else
                        callback.onComplete(null);
                });
            });
        });
    }

    // TODO: Implement these methods when needed
    // public void activateEquipment(String id) { }
    // public void upgradeEquipment(String id) { }
    // public void getUserEquipment() { }

    public String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


}
