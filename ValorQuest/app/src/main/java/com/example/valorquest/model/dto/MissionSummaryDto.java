package com.example.valorquest.model.dto;

import java.util.Date;
import java.util.List;

public class MissionSummaryDto {
    public String missionId;
    public int currentBossHp;
    public int originalBossHp;
    public Date startDate;
    public Date endDate;
    public List<UserContributionDto> contributions;

    public MissionSummaryDto(){}
}