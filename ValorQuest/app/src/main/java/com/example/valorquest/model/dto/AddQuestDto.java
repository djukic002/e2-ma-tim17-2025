package com.example.valorquest.model.dto;

import com.example.valorquest.model.enums.Difficulty;
import com.example.valorquest.model.enums.Importance;
import com.example.valorquest.model.enums.RepeatingUnit;

public class AddQuestDto {
    public String userId;

    public String name;
    public String description;
    public Difficulty difficulty;
    public Importance importance;
    public int categoryId;
    public boolean isRepeating;
    public String dueDate;
    public String startDate;
    public String endDate;
    public int repeatingInterval;
    public RepeatingUnit unit;
    public AddQuestDto(){}

    @Override
    public String toString() {
        return "AddQuestDto{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", difficulty=" + difficulty +
                ", importance=" + importance +
                ", categoryId=" + categoryId +
                ", isRepeating=" + isRepeating +
                ", dueDate='" + dueDate + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", repeatingInterval=" + repeatingInterval +
                ", unit=" + unit +
                '}';
    }
}
