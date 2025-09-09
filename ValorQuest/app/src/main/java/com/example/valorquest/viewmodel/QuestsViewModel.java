package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.QuestRepository;
import com.example.valorquest.model.QuestWithExecutions;

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
    public LiveData<List<QuestWithExecutions>> getAllQuestsWithExecutionsForUser(String userId) {
        return questRepository.getAllQuestsWithExecutionsForUser(userId);
    }
}
