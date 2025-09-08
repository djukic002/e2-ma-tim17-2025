package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.QuestRepository;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddQuestViewModel extends ViewModel {

    private final QuestRepository repository;

    @Inject
    public AddQuestViewModel(QuestRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<String>> addQuest(AddQuestDto dto) {
        return repository.addQuest(dto);
    }
}