package com.example.valorquest.model;

import com.example.valorquest.model.enums.AllianceMissionStatus;

import java.util.Date;

public class AllianceMission {
    private String id;
    private String allianceId;
    private Date startDate;
    private AllianceMissionStatus status;
    private int originalBossHp;
    private int currentBossHp;
    public AllianceMission() {}

    public AllianceMission(String id, String allianceId, AllianceMissionStatus status, int originalBossHp) {
        this.id = id;
        this.allianceId = allianceId;
        this.startDate = new Date();
        this.status = status;
        this.originalBossHp = originalBossHp;
        this.currentBossHp = originalBossHp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public AllianceMissionStatus getStatus() { return status; }
    public void setStatus(AllianceMissionStatus status) { this.status = status; }

    public int getOriginalBossHp() { return originalBossHp; }
    public void setOriginalBossHp(int originalBossHp) { this.originalBossHp = originalBossHp; }

    public int getCurrentBossHp() { return currentBossHp; }
    public void setCurrentBossHp(int currentBossHp) { this.currentBossHp = currentBossHp; }
}

