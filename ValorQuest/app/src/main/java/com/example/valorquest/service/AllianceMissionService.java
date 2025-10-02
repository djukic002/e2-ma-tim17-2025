package com.example.valorquest.service;

import android.util.Log;

import com.example.valorquest.data.repositories.AllianceMissionRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.MissionContributionRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Alliance;
import com.example.valorquest.model.AllianceMission;
import com.example.valorquest.model.MissionContribution;
import com.example.valorquest.model.User;
import com.example.valorquest.model.enums.AllianceMissionStatus;
import com.example.valorquest.model.enums.MissionContributionType;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AllianceMissionService {
    private final AllianceService allianceService;
    private final UserRepository userRepository;

    private final AllianceRepository allianceRepository;
    @Inject
    public AllianceMissionService(AllianceService allianceService, UserRepository userRepository, AllianceRepository allianceRepository) {
        this.allianceService = allianceService;
        this.userRepository = userRepository;
        this.allianceRepository = allianceRepository;
    }

    /**
     * Gets the active mission for the given alliance.
     */
    public void getActiveMission(String allianceId, RepositoryCallback<AllianceMission> callback) {
        AllianceMissionRepository missionRepository = new AllianceMissionRepository(allianceId);

        missionRepository.getAll(missions -> {
            if (missions != null) {
                for (AllianceMission mission : missions) {
                    if (mission.getStatus() == AllianceMissionStatus.ACTIVE) {
                        callback.onComplete(mission);
                        return;
                    }
                }
            }
            callback.onComplete(null);
        });
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Creates a new mission if no active one exists.
     */
    public void createMission(RepositoryCallback<AllianceMission> callback) {
        String currentUserId = getCurrentUserId();
        Log.d("AllianceMissionService", "createMission: Starting for userId = " + currentUserId);

        userRepository.getById(currentUserId, user -> {
            if (user == null) {
                Log.w("AllianceMissionService", "User not found in repository.");
                callback.onComplete(null);
                return;
            }

            String allianceId = user.getAllianceId();
            if (allianceId == null) {
                Log.w("AllianceMissionService", "User is not in an alliance. Cannot create mission.");
                callback.onComplete(null);
                return;
            }

            // Step 2: Fetch the alliance
            allianceRepository.getById(allianceId, alliance -> {
                if (alliance == null) {
                    Log.w("AllianceMissionService", "Alliance not found for allianceId = " + allianceId);
                    callback.onComplete(null);
                    return;
                }

                if (!alliance.getLeaderId().equals(currentUserId)) {
                    Log.w("AllianceMissionService", "User is not the leader of alliance " + allianceId);
                    callback.onComplete(null);
                    return;
                }

                // Step 3: Check if an active mission exists
                AllianceMissionRepository missionRepository = new AllianceMissionRepository(allianceId);
                Log.d("AllianceMissionService", "Checking if active mission exists for allianceId = " + allianceId);

                getActiveMission(allianceId, active -> {
                    if (active != null) {
                        Log.w("AllianceMissionService", "Active mission already exists with id = " + active.getId());
                        callback.onComplete(null);
                    } else {
                        Log.d("AllianceMissionService", "No active mission found. Creating a new one...");

                        String missionId = UUID.randomUUID().toString();
                        AllianceMission newMission = new AllianceMission();
                        newMission.setId(missionId);
                        newMission.setAllianceId(allianceId);
                        newMission.setStatus(AllianceMissionStatus.ACTIVE);
                        newMission.setStartDate(new Date());
                        newMission.setCurrentBossHp(100 * alliance.getMembers().size());

                        Log.d("AllianceMissionService", "Saving new mission: id=" + missionId);

                        missionRepository.save(missionId, newMission, task -> {
                            if (task.isSuccessful()) {
                                Log.i("AllianceMissionService", "Mission created successfully with id = " + missionId);
                                callback.onComplete(newMission);
                            } else {
                                Log.e("AllianceMissionService", "Failed to create mission.", task.getException());
                                callback.onComplete(null);
                            }
                        });
                    }
                });
            });
        });
    }

    public void contribute(MissionContributionType actionType, RepositoryCallback<Boolean> callback) {
        String userId = getCurrentUserId();
        Log.d("AllianceMissionService", "contribute: Starting contribution for userId=" + userId + ", actionType=" + actionType);

        userRepository.getById(userId, user -> {
            if (user == null || user.getAllianceId() == null) {
                Log.w("AllianceMissionService", "contribute: User has no alliance. Cannot contribute.");
                callback.onComplete(false);
                return;
            }

            String allianceId = user.getAllianceId();
            AllianceMissionRepository missionRepository = new AllianceMissionRepository(allianceId);

            getActiveMission(allianceId, mission -> {
                if (mission == null) {
                    Log.w("AllianceMissionService", "contribute: No active mission found for allianceId=" + allianceId);
                    callback.onComplete(false);
                    return;
                }
                String missionId = mission.getId();
                MissionContributionRepository contributionRepository = new MissionContributionRepository(allianceId, missionId);

                canContribute(contributionRepository, userId, actionType, allowed -> {
                    if (!allowed) {
                        Log.w("AllianceMissionService", "contribute: User is not allowed to contribute to missionId=" + missionId + " with actionType=" + actionType);
                        callback.onComplete(false);
                        return;
                    }

                    int hpDealt = MissionContribution.calculateHpForAction(actionType);
                    Log.d("AllianceMissionService", "contribute: Calculated hpDealt=" + hpDealt);

                    String contributionId = UUID.randomUUID().toString();
                    MissionContribution contribution = new MissionContribution(contributionId, missionId, userId, hpDealt, actionType);

                    contributionRepository.save(contributionId, contribution, task -> {
                        if (task.isSuccessful()) {
                            int newHp = Math.max(0, mission.getCurrentBossHp() - hpDealt);
                            mission.setCurrentBossHp(newHp);

                            missionRepository.save(missionId, mission, updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Log.i("AllianceMissionService", "contribute: Contribution saved, mission updated. Remaining HP=" + newHp);
                                    callback.onComplete(true);
                                } else {
                                    Log.e("AllianceMissionService", "contribute: Failed to update mission HP", updateTask.getException());
                                    callback.onComplete(false);
                                }
                            });
                        } else {
                            Log.e("AllianceMissionService", "contribute: Failed to save contribution", task.getException());
                            callback.onComplete(false);
                        }
                    });
                });
            });
        });
    }

    public void canContribute(MissionContributionRepository contributionRepo,
                              String userId,
                              MissionContributionType actionType,
                              AllianceService.BooleanCheckCallback callback) {
        int limit = MissionContribution.getMaxPerAction(actionType);

        contributionRepo.countUserContributions(userId, actionType, limit, allowed -> {
            callback.onResult(allowed);
        });
    }
}