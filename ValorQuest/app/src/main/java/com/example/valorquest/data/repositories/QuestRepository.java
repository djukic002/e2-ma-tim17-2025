package com.example.valorquest.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.data.local.QuestDao;
import com.example.valorquest.model.Quest;
import com.example.valorquest.model.QuestExecution;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QuestRepository {
    private final QuestDao questDao;

    @Inject
    public QuestRepository(QuestDao questDao) {
        this.questDao = questDao;
    }

    public LiveData<Result<String>> addQuest(AddQuestDto dto) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Build Quest entity
                Quest quest = new Quest(
                        dto.userId,
                        dto.name,
                        dto.description,
                        dto.difficulty,
                        dto.importance,
                        dto.categoryId
                );

//                long questId = questDao.insertQuest(quest);
//
//                // If repeating or has dates, create executions
//                if (dto.dueDate != null || dto.isRepeating) {
//                    LocalDateTime start = dto.startDate != null ? LocalDateTime.parse(dto.startDate) : null;
//                    LocalDateTime end = dto.endDate != null ? LocalDateTime.parse(dto.endDate) : null;
//
//                    // Example: just add 1 execution for now
//                    QuestExecution execution = new QuestExecution(
//                            start != null ? start : LocalDateTime.now(),
//                            QuestStatus.PENDING,
//                            (int) questId
//                    );
//
//                    questDao.insertExecution(execution);
//                }

                result.postValue(Result.success("Quest inserted successfully"));

            } catch (Exception e) {
                result.postValue(Result.error("Error inserting quest: " + e.getMessage()));
            }
        });

        return result;
    }
}