package com.example.valorquest.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.valorquest.model.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insertCategory(Category category);

    @Update
    void updateCategory(Category category);

    @Query("SELECT * FROM categories WHERE userId = :userId")
    List<Category> getCategoriesForUser(String userId);

    @Query("SELECT * FROM categories WHERE userId = :userId AND color = :color LIMIT 1")
    Category getCategoryByColor(String userId, String color);

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    Category getCategoryById(int categoryId);
}
