package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.CategoryRepository;
import com.example.valorquest.data.repositories.QuestRepository;
import com.example.valorquest.model.Category;
import com.example.valorquest.model.QuestWithExecutions;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;
import com.example.valorquest.model.dto.DetailedQuestExecutionDto;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuestsViewModel extends ViewModel {
    private final QuestRepository questRepository;

    private final CategoryRepository categoryRepository;

    @Inject
    public QuestsViewModel(QuestRepository questRepository, CategoryRepository categoryRepository) {
        this.questRepository = questRepository;
        this.categoryRepository = categoryRepository;
    }

    public LiveData<List<QuestWithExecutions>> getAllQuestsWithExecutions() {
        return questRepository.getAllQuestsWithExecutions();
    }
    public LiveData<List<DetailedQuestExecutionDto>> getDetailedQuestExecutionsForUser(String userId) {
        return questRepository.getDetailedExecutionsForUser(userId);
    }

    public LiveData<DetailedQuestExecutionDto> getDetailedExecutionById(int executionId) {
        return questRepository.getDetailedExecutionById(executionId);
    }
    public LiveData<Result<String>> deleteQuest(int questId, int executionId) {
        return questRepository.deleteQuestsWithExecutions(questId, executionId);
    }

    public LiveData<Result<String>> addQuest(AddQuestDto dto) {
        return questRepository.addQuest(dto);
    }

    public LiveData<Result<String>> updateQuest(int questExecutionId,AddQuestDto dto) {
        return questRepository.updateQuest(questExecutionId,dto);
    }

    public LiveData<List<Category>> getCategoriesForUser(String userId){
        return categoryRepository.getCategoriesForUser(userId);
    }

}
