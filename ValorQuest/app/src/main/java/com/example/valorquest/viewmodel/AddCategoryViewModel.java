package com.example.valorquest.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.CategoryRepository;
import com.example.valorquest.model.Category;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddCategoryViewModel extends ViewModel {
    private final CategoryRepository repository;

    @Nullable
    private String selectedColorHex;

    @Inject
    public AddCategoryViewModel(CategoryRepository repository) {
        this.repository = repository;
    }

    public void setSelectedColorHex(@Nullable String colorHex) {
        this.selectedColorHex = colorHex;
    }

    @Nullable
    public String getSelectedColorHex() {
        return selectedColorHex;
    }

    public void addCategory(String userId, String name, String colorHex) {
        Category category = new Category(userId, name, colorHex);
        repository.addCategory(category);
    }
}