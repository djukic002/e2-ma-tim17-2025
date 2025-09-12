package com.example.valorquest.data.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.data.local.QuestDao;
import com.example.valorquest.model.Quest;
import com.example.valorquest.model.QuestExecution;
import com.example.valorquest.model.QuestWithExecutions;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;
import com.example.valorquest.model.dto.DetailedQuestExecutionDto;
import com.example.valorquest.model.enums.QuestStatus;
import com.example.valorquest.model.enums.RepeatingUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QuestRepository {
    private final QuestDao questDao;

    @Inject
    public QuestRepository(QuestDao questDao) {
        this.questDao = questDao;
    }

    public LiveData<List<QuestWithExecutions>> getAllQuestsWithExecutions() {
        return questDao.getAllQuestsWithExecutions();
    }

    public LiveData<List<QuestWithExecutions>> getAllQuestsWithExecutionsForUser(String userId) {
        return questDao.getAllQuestsWithExecutionsForUser(userId);
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
                        dto.categoryId,
                        dto.isRepeating
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

    public LiveData<List<DetailedQuestExecutionDto>> getDetailedExecutionsForUser(String userId) {
        return Transformations.map(
                questDao.getAllQuestsWithExecutionsForUser(userId),
                questList -> {
                    List<DetailedQuestExecutionDto> detailedList = new ArrayList<>();
                    for (QuestWithExecutions questWithExec : questList) {
                        for (QuestExecution exec : questWithExec.executions) {
                            detailedList.add(
                                    new DetailedQuestExecutionDto(
                                            questWithExec.quest, exec, questWithExec.category
                                    )
                            );
                        }
                    }
                    return detailedList;
                }
        );
    }

    public LiveData<DetailedQuestExecutionDto> getDetailedExecutionById(int executionId) {
        return Transformations.map(
                questDao.getQuestWithSingleExecution(executionId),
                questWithExec -> {
                    if (questWithExec == null || questWithExec.executions == null) {
                        return null;
                    }

                    for (QuestExecution exec : questWithExec.executions) {
                        if (exec.getId() == executionId) {
                            return new DetailedQuestExecutionDto(
                                    questWithExec.quest,
                                    exec,
                                    questWithExec.category
                            );
                        }
                    }
                    return null;
                }
        );
    }

    public LiveData<Result<String>> deleteQuestsWithExecutions(int questId, int executionId){
        MutableLiveData<Result<String>> result = new MutableLiveData<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                QuestWithExecutions questExec = questDao.getQuestWithExecutions(questId);

                if(questExec.executions.size() > 1 && !questExec.quest.isRepeating()){
                    throw new Exception("Fatal error, non repeatable quest has more that one execution");
                }

                if(questExec.quest.isRepeating()){
                    int deletedCnt = 0;
                    for (QuestExecution exec: questExec.executions) {
                        if((exec.getStatus() != QuestStatus.ACTIVE ||
                                exec.getStatus() != QuestStatus.PAUSED) &&
                                exec.getDate().isAfter(LocalDateTime.now())){
                            questDao.deleteExecution(exec);
                            deletedCnt++;
                        }
                    }

                    if(deletedCnt == questExec.executions.size()){
                        questDao.deleteQuest(questExec.quest);
                    }
                }else{
                    QuestExecution exec = questExec.executions.get(0);

                    questDao.deleteExecution(exec);
                    questDao.deleteQuest(questExec.quest);
                }

                result.postValue(Result.success("Quest deleted successfully"));

            } catch (Exception e) {
                result.postValue(Result.error("Error deleting quests: " + e.getMessage()));
            }
        });

        return result;
    }


}