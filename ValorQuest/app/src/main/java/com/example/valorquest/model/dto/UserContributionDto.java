package com.example.valorquest.model.dto;

public class UserContributionDto {
    public String userId;
    public String username;
    public int totalHpDealt;
    public UserContributionDto(String userId, String username, int totalHpDealt) {
        this.userId = userId;
        this.username = username;
        this.totalHpDealt = totalHpDealt;
    }
}
