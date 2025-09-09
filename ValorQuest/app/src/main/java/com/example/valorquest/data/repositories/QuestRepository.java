package com.example.valorquest.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.data.local.QuestDao;
import com.example.valorquest.model.Quest;
import com.example.valorquest.model.QuestExecution;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;
import com.example.valorquest.model.enums.QuestStatus;
import com.example.valorquest.model.enums.RepeatingUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
                Quest quest = new Quest(
                        dto.userId,
                        dto.name,
                        dto.description,
                        dto.difficulty,
                        dto.importance,
                        dto.categoryId
                );

                long questId = questDao.insertQuest(quest);

                if(!dto.isRepeating && dto.dueDate != null){
                    LocalDate date = LocalDate.parse(dto.dueDate);
                    LocalDateTime dateTime = date.atStartOfDay();

                    QuestExecution execution = new QuestExecution(dateTime, QuestStatus.ACTIVE, (int)questId);
                    questDao.insertExecution(execution);
                }
                else if(dto.isRepeating && dto.startDate != null && dto.endDate != null){
                    LocalDate start = LocalDate.parse(dto.startDate);
                    LocalDateTime startTime = start.atStartOfDay();

                    LocalDate end = LocalDate.parse(dto.endDate);
                    LocalDateTime endTime = end.atStartOfDay();

                    LocalDateTime current = startTime;
                    while (!current.isAfter(endTime)) {
                        QuestExecution execution = new QuestExecution(current, QuestStatus.ACTIVE, (int)questId);
                        questDao.insertExecution(execution);

                        if (dto.unit == RepeatingUnit.DAILY) {
                            current = current.plusDays(dto.repeatingInterval);
                        } else if (dto.unit == RepeatingUnit.WEEKLY) {
                            current = current.plusWeeks(dto.repeatingInterval);
                        } else {
                            throw new IllegalArgumentException("Unknown repeating unit: " + dto.unit);
                        }
                    }
                }

                result.postValue(Result.success("Quest inserted successfully"));

            } catch (Exception e) {
                Log.d("AddQuestFragment", e.getMessage());
                result.postValue(Result.error("Error inserting quest: " + e.getMessage()));
            }
        });

        return result;
    }
}