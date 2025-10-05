package com.example.valorquest.model;

import com.example.valorquest.model.enums.MissionContributionType;

import java.util.Date;

public class MissionContribution {
    private String id;
    private String missionId;
    private String userId;
    private int hpDealt;
    private MissionContributionType actionType;
    private Date occurredAt;
    public MissionContribution() {}

    public MissionContribution(String id, String missionId, String userId, int hpDealt,
                               MissionContributionType actionType) {
        this.id = id;
        this.missionId = missionId;
        this.userId = userId;
        this.hpDealt = hpDealt;
        this.actionType = actionType;
        this.occurredAt = new Date();
    }
    public static int calculateHpForAction(MissionContributionType actionType) {
        if (actionType == null) return 0;

        switch (actionType) {
            case SHOPPING:
            case BOSS_HIT:
                return 2;
            case EASY_QUEST:
                return 1;
            case HARD_QUEST:
            case MESSAGE:
                return 4;
            case ALL_QUEST:
                return 10;
            default:
                return 0;
        }
    }
    public static int getMaxPerAction(MissionContributionType actionType) {
        switch (actionType) {
            case SHOPPING: return 5;
            case BOSS_HIT:
            case EASY_QUEST:
                return 10;
            case HARD_QUEST: return 6;
            case ALL_QUEST:
            case MESSAGE:
            default: return Integer.MAX_VALUE; // other types handled differently
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMissionId() { return missionId; }
    public void setMissionId(String missionId) { this.missionId = missionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getHpDealt() { return hpDealt; }
    public void setHpDealt(int hpDealt) { this.hpDealt = hpDealt; }

    public MissionContributionType getActionType() { return actionType; }
    public void setActionType(MissionContributionType actionType) { this.actionType = actionType; }

    public Date getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Date occurredAt) { this.occurredAt = occurredAt; }
}