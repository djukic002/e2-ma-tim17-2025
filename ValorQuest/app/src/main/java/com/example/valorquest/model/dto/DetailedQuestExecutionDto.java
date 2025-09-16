package com.example.valorquest.model.dto;

import com.example.valorquest.model.Category;
import com.example.valorquest.model.Quest;
import com.example.valorquest.model.QuestExecution;

import java.time.LocalDateTime;

public class DetailedQuestExecutionDto {
    public int executionId;
    public int questId;
    public String questName;

    public String description;

    public String importance;
    public String difficulty;
    public String status;
    public LocalDateTime date;
    public String categoryName;
    public String categoryColor;
    public boolean isRepeating;
    public DetailedQuestExecutionDto(){}

    public DetailedQuestExecutionDto(Quest quest, QuestExecution qe, Category category){
        questId = quest.getId();
        questName = quest.getName();
        description = quest.getDescription();
        importance = quest.getImportance().toString();
        difficulty = quest.getDifficulty().toString();
        isRepeating = quest.isRepeating();

        status = qe.getStatus().toString();
        executionId = qe.getId();
        date = qe.getDate();

        categoryName = category.getName();
        categoryColor = category.getColor();
    }
}