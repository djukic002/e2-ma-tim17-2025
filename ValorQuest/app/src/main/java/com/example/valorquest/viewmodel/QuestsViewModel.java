package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.CategoryRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.User;
import com.example.valorquest.service.QuestService;
import com.example.valorquest.model.Category;
import com.example.valorquest.model.QuestWithExecutions;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;
import com.example.valorquest.model.dto.DetailedQuestExecutionDto;
import com.example.valorquest.model.enums.QuestStatus;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuestsViewModel extends ViewModel {
    private final QuestService questService;
    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;
    @Inject
    public QuestsViewModel(QuestService questService, CategoryRepository categoryRepository) {
        this.questService = questService;
        this.categoryRepository = categoryRepository;
        this.userRepository = new UserRepository();
    }

    public LiveData<List<QuestWithExecutions>> getAllQuestsWithExecutions() {
        return questService.getAllQuestsWithExecutions();
    }
    public LiveData<List<DetailedQuestExecutionDto>> getDetailedQuestExecutionsForUser(String userId, boolean timeFilter) {
        return questService.getDetailedExecutionsForUser(userId, timeFilter);
    }

    public LiveData<DetailedQuestExecutionDto> getDetailedExecutionById(int executionId) {
        return questService.getDetailedExecutionById(executionId);
    }
    public LiveData<Result<String>> deleteQuest(int questId, int executionId) {
        return questService.deleteQuestsWithExecutions(questId, executionId);
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        userRepository.getById(userId, user -> {
            userLiveData.postValue(user);
        });
        return userLiveData;
    }

    public LiveData<Result<String>> addQuest(AddQuestDto dto) {
        return questService.addQuest(dto);
    }

    public LiveData<Result<String>> updateQuest(int questExecutionId,AddQuestDto dto) {
        return questService.updateQuest(questExecutionId,dto);
    }

    public LiveData<List<Category>> getCategoriesForUser(String userId){
        return categoryRepository.getCategoriesForUser(userId);
    }

    public LiveData<Result<String>> changeActiveQuestStatus(int questId, int executionId, QuestStatus status, User user){
        return questService.changeActiveQuestStatus(questId,executionId,status, user);
    }

    public LiveData<Result<String>> unpauseQuest(int questId){
        return questService.unpauseQuest(questId);
    }
}
