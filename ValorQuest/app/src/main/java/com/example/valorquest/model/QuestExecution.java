package com.example.valorquest.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.valorquest.model.enums.QuestStatus;

import java.time.LocalDateTime;

@Entity(
        tableName = "quest_executions",
        foreignKeys = @ForeignKey(
                entity = Quest.class,
                parentColumns = "id",
                childColumns = "questId",
                onDelete = ForeignKey.CASCADE
        )
)
public class QuestExecution {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int questId;
    private LocalDateTime date;
    private QuestStatus status;
    public int xpEarned;
    public LocalDateTime questCompleted;

    public boolean quotaExceeded;
    public QuestExecution(LocalDateTime date, QuestStatus status, int questId) {
        this.date = date;
        this.status = status;
        this.questId = questId;
        this.xpEarned = 0;
        this.quotaExceeded = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public QuestStatus getStatus() {
        return status;
    }
    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public int getQuestId() {
        return questId;
    }
    public void setQuestId(int questId) {
        this.questId = questId;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    public LocalDateTime getQuestCompleted() {
        return questCompleted;
    }

    public void setQuestCompleted(LocalDateTime questCompleted) {
        this.questCompleted = questCompleted;
    }

    public boolean isQuotaExceeded() {
        return quotaExceeded;
    }

    public void setQuotaExceeded(boolean quotaExceeded) {
        this.quotaExceeded = quotaExceeded;
    }
}