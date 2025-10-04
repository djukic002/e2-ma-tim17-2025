package com.example.valorquest.service;

import android.util.Log;

import com.example.valorquest.data.repositories.AllianceMissionRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.MissionContributionRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.AllianceMission;
import com.example.valorquest.model.MissionContribution;
import com.example.valorquest.model.dto.MissionSummaryDto;
import com.example.valorquest.model.dto.UserContributionDto;
import com.example.valorquest.model.enums.AllianceMissionStatus;
import com.example.valorquest.model.enums.MissionContributionType;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AllianceMissionService {
    private final UserRepository userRepository;
    private final AllianceRepository allianceRepository;
    @Inject
    public AllianceMissionService() {
        this.userRepository = new UserRepository();
        this.allianceRepository = new AllianceRepository();
    }

    public void isUserLeader(String userId, AllianceService.BooleanCheckCallback callback) {
        userRepository.getById(userId, user -> {
            allianceRepository.getById(user.getAllianceId(), alliance -> {
                Log.d("KURCINELA", alliance.getLeaderId() + " - " + userId);
                boolean result = Objects.equals(alliance.getLeaderId(), userId);
                callback.onResult(result);
            });
        });

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

    public String getCurrentUserId() {
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

                AllianceMissionRepository missionRepository = new AllianceMissionRepository(allianceId);
                Log.d("AllianceMissionService", "Checking if active mission exists for allianceId = " + allianceId);

                getActiveMission(allianceId, active -> {
                    if (active != null) {
                        Log.w("AllianceMissionService", "Active mission already exists with id = " + active.getId());
                        callback.onComplete(null);
                    } else {
                        Log.d("AllianceMissionService", "No active mission found. Creating a new one...");

                        String missionId = UUID.randomUUID().toString();
                        AllianceMission newMission = new AllianceMission(missionId, allianceId, AllianceMissionStatus.ACTIVE, 100 * alliance.getMembers().size());

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

        if (actionType == MissionContributionType.ALL_QUEST) {
            callback.onResult(true);
            return;
        }

        if (actionType == MissionContributionType.MESSAGE) {
            contributionRepo.hasUserContributedToday(userId, actionType, allowed -> {
                // allowed = false means user already contributed today
                callback.onResult(!allowed);
            });
            return;
        }

        int limit = MissionContribution.getMaxPerAction(actionType);
        contributionRepo.countUserContributions(userId, actionType, limit, allowed -> {
            callback.onResult(allowed);
        });
    }

    public void getActiveMissionSummary(RepositoryCallback<MissionSummaryDto> callback) {
        String currentUserId = getCurrentUserId();

        userRepository.getById(currentUserId, user -> {
            if (user == null || user.getAllianceId() == null) {
                Log.w("AllianceMissionService", "getActiveMissionSummary: User has no alliance.");
                callback.onComplete(null);
                return;
            }

            String allianceId = user.getAllianceId();

            getActiveMission(allianceId, mission -> {
                if (mission == null) {
                    Log.w("AllianceMissionService", "getActiveMissionSummary: No active mission found for allianceId=" + allianceId);
                    callback.onComplete(null);
                    return;
                }

                String missionId = mission.getId();
                MissionContributionRepository contributionRepository =
                        new MissionContributionRepository(allianceId, missionId);

                contributionRepository.getAll(contributions -> {
                    if (contributions == null) {
                        callback.onComplete(null);
                        return;
                    }

                    Map<String, Integer> hpByUser = new HashMap<>();
                    for (MissionContribution c : contributions) {
                        hpByUser.merge(c.getUserId(), c.getHpDealt(), Integer::sum);
                    }

                    List<UserContributionDto> userDtos = new ArrayList<>();

                    if (!hpByUser.isEmpty()) {
                        for (Map.Entry<String, Integer> entry : hpByUser.entrySet()) {
                            String userId = entry.getKey();
                            int totalHp = entry.getValue();

                            userRepository.getById(userId, contribUser -> {
                                if (contribUser != null) {
                                    userDtos.add(new UserContributionDto(
                                            contribUser.getId(),
                                            contribUser.getUsername(),
                                            totalHp
                                    ));
                                }

                                // When all users are processed -> build summary
                                if (userDtos.size() == hpByUser.size()) {
                                    MissionSummaryDto summary = buildMissionSummary(mission, userDtos);
                                    callback.onComplete(summary);
                                }
                            });
                        }
                    } else {
                        MissionSummaryDto summary = buildMissionSummary(mission, new ArrayList<>());
                        callback.onComplete(summary);
                    }
                });
            });
        });
    }

    private MissionSummaryDto buildMissionSummary(AllianceMission mission, List<UserContributionDto> contributions) {
        MissionSummaryDto summary = new MissionSummaryDto();
        summary.missionId = mission.getId();
        summary.currentBossHp = mission.getCurrentBossHp();
        summary.originalBossHp = mission.getOriginalBossHp();
        summary.startDate = mission.getStartDate();

        LocalDateTime start = mission.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime end = start.plusWeeks(2);

        summary.endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());

        summary.contributions = contributions;
        return summary;
    }


    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public void startAutoFailTask() {
        Runnable task = () -> {
            try {
                Log.d("AllianceMissionService", "Running scheduled mission check...");
                checkAndUpdateMissionsStatus();
            } catch (Exception e) {
                Log.e("AllianceMissionService", "Error during scheduled mission check: " + e.getMessage(), e);
            }
        };

        scheduler.scheduleWithFixedDelay(task, 0, 5, TimeUnit.MINUTES);
    }

    public void checkAndUpdateMissionsStatus() {
        allianceRepository.getAll(alliances -> {
            if (alliances == null) return;

            for (var alliance : alliances) {
                String allianceId = alliance.getId();
                AllianceMissionRepository missionRepo = new AllianceMissionRepository(allianceId);

                missionRepo.getAll(missions -> {
                    if (missions == null) return;

                    for (AllianceMission mission : missions) {
                        boolean updated = false;

                        LocalDateTime start = mission.getStartDate().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        LocalDateTime end = start.plusWeeks(2);
                        Date now = new Date();

                        if (mission.getStatus() == AllianceMissionStatus.ACTIVE) {
                            if (mission.getCurrentBossHp() <= 0) {
                                mission.setStatus(AllianceMissionStatus.COMPLETED);
                                // dodavanje nagrade korisnicima misija
                                updated = true;
                            } else if (now.after(Date.from(end.atZone(ZoneId.systemDefault()).toInstant()))) {
                                mission.setStatus(AllianceMissionStatus.FAILED);
                                updated = true;
                            }

                            if (updated) {
                                missionRepo.save(mission.getId(), mission, task -> {
                                    if (task.isSuccessful()) {
                                        Log.i("AllianceMissionService", "Mission " + mission.getId() +
                                                " status updated to " + mission.getStatus());
                                    } else {
                                        Log.e("AllianceMissionService", "Failed to update mission " + mission.getId(), task.getException());
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }
}