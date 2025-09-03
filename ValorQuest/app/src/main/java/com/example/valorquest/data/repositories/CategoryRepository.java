package com.example.valorquest.data.repositories;

import androidx.lifecycle.LiveData;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.data.local.CategoryDao;
import com.example.valorquest.model.Category;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CategoryRepository {
    private final CategoryDao categoryDao;

    @Inject
    public CategoryRepository(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public LiveData<List<Category>> getCategoriesForUser(String userId) {
        return categoryDao.getCategoriesForUser(userId);
    }

    public void addCategory(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Category existing = categoryDao.getCategoryByColor(category.getUserId(), category.getColor());
            if (existing == null) {
                categoryDao.insertCategory(category);
            }
        });
    }

    public void changeCategoryColor(int categoryId, String userId, String newColor) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Category existing = categoryDao.getCategoryByColor(userId, newColor);
            if (existing == null) {
                Category category = categoryDao.getCategoryById(categoryId);
                if (category != null) {
                    category.setColor(newColor);
                    categoryDao.updateCategory(category);
                }
            }
        });
    }
}