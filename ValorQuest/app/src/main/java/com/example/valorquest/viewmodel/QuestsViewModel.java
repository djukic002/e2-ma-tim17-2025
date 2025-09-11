package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.QuestRepository;
import com.example.valorquest.model.QuestWithExecutions;
import com.example.valorquest.model.dto.DetailedQuestExecutionDto;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuestsViewModel extends ViewModel {
    private final QuestRepository questRepository;

    @Inject
    public QuestsViewModel(QuestRepository questRepository) {
        this.questRepository = questRepository;
    }

    public LiveData<List<QuestWithExecutions>> getAllQuestsWithExecutions() {
        return questRepository.getAllQuestsWithExecutions();
    }
    public LiveData<List<DetailedQuestExecutionDto>> getDetailedQuestExecutionsForUser(String userId) {
        return questRepository.getDetailedExecutionsForUser(userId);
    }
}
