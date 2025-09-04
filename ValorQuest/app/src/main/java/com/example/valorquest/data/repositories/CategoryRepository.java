package com.example.valorquest.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.data.local.CategoryDao;
import com.example.valorquest.model.Category;
import com.example.valorquest.model.Result;

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

    public LiveData<Result<String>> addCategory(Category category) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Category existing = categoryDao.getCategoryByColor(category.getUserId(), category.getColor());
            if (existing == null) {
                categoryDao.insertCategory(category);
                result.postValue(Result.success("Category inserted successfully"));
            } else {
                result.postValue(Result.error("Color already exists"));
            }
        });

        return result;
    }
}