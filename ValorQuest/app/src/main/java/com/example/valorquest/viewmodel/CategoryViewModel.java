package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.CategoryRepository;
import com.example.valorquest.model.Category;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CategoryViewModel extends ViewModel {

    private final CategoryRepository repository;
    private final MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();

    @Inject
    public CategoryViewModel(CategoryRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Category>> getCategories() {
        return categoriesLiveData;
    }

//    public void loadCategories(String userId) {
//        categoriesLiveData.setValue(repository.getCategoriesForUser(userId));
//    }
//
//    public boolean addCategory(Category category) {
//        boolean result = repository.addCategory(category);
//        if (result) loadCategories(category.getUserId());
//        return result;
//    }
//
//    public boolean changeCategoryColor(int categoryId, String userId, String newColor) {
//        boolean result = repository.changeCategoryColor(categoryId, userId, newColor);
//        if (result) loadCategories(userId);
//        return result;
//    }
}