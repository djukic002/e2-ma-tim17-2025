package com.example.valorquest.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.valorquest.model.enums.Difficulty;
import com.example.valorquest.model.enums.Importance;

import javax.annotation.Nullable;

@Entity(
        tableName = "quests",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.NO_ACTION // brisanje kategorije svakako nije moguce
        )
)
public class Quest {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userId; // Firebase UID
    private String name;
    @Nullable
    private String description;
    private Difficulty difficulty;
    private Importance importance;
    private int categoryId;
    private boolean isRepeating;

    public Quest(String userId, String name, @Nullable String description, Difficulty difficulty, Importance importance, int categoryId, boolean isRepeating) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.importance = importance;
        this.categoryId = categoryId;
        this.isRepeating = isRepeating;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }
    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Importance getImportance() {
        return importance;
    }
    public void setImportance(Importance importance) {
        this.importance = importance;
    }

    public int getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isRepeating(){return isRepeating;}

    public void setIsRepeating(boolean isRepeating){ this.isRepeating = isRepeating;}
}
