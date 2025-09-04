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
}