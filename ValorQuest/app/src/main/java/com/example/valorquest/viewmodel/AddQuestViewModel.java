package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.CategoryRepository;
import com.example.valorquest.data.repositories.QuestRepository;
import com.example.valorquest.model.Category;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddQuestViewModel extends ViewModel {

    private final QuestRepository questRepository;

    private final CategoryRepository categoryRepository;

    @Inject
    public AddQuestViewModel(QuestRepository questRepository, CategoryRepository categoryRepository) {
        this.questRepository = questRepository;
        this.categoryRepository = categoryRepository;
    }

    public LiveData<Result<String>> addQuest(AddQuestDto dto) {
        return questRepository.addQuest(dto);
    }

    public LiveData<List<Category>> getCategoriesForUser(String userId){
        return categoryRepository.getCategoriesForUser(userId);
    }
}