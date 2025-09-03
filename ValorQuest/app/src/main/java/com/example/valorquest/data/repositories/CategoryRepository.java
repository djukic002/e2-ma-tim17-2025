package com.example.valorquest.data.repositories;

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

    public boolean addCategory(Category category) {
        Category existing = categoryDao.getCategoryByColor(category.getUserId(), category.getColor());
        if (existing != null) return false; // duplicate color not allowed
        categoryDao.insertCategory(category);
        return true;
    }

    public boolean changeCategoryColor(int categoryId, String userId, String newColor) {
        Category existing = categoryDao.getCategoryByColor(userId, newColor);
        if (existing != null) return false;

        Category category = categoryDao.getCategoryById(categoryId);
        if (category != null) {
            category.setColor(newColor);
            categoryDao.updateCategory(category);
            return true;
        }
        return false;
    }

    public List<Category> getCategoriesForUser(String userId) {
        return categoryDao.getCategoriesForUser(userId);
    }
}