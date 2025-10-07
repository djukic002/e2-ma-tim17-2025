package com.example.valorquest.model.dto;

import androidx.room.ColumnInfo;
public class CategoryQuestCountDTO {

    @ColumnInfo(name = "categoryName")
    private String categoryName;

    @ColumnInfo(name = "color")
    private String color;

    @ColumnInfo(name = "count")
    private int count;

    public CategoryQuestCountDTO(String categoryName, String color, int count) {
        this.categoryName = categoryName;
        this.color = color;
        this.count = count;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getColor() {
        return color;
    }

    public int getCount() {
        return count;
    }
}