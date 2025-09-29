package com.example.valorquest.service;

import android.util.Log;
import android.widget.Switch;

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
import com.example.valorquest.model.enums.Difficulty;
import com.example.valorquest.model.enums.Importance;
import com.example.valorquest.model.enums.QuestStatus;
import com.example.valorquest.model.enums.RepeatingUnit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QuestService {
    private final QuestDao questDao;
    private final UserService userService;
    private final BossService bossService;
    @Inject
    public QuestService(QuestDao questDao, UserService userService, BossService bossService) {
        this.questDao = questDao;
        this.userService = userService;
        this.bossService = bossService;
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
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                if(!dto.isRepeating && dto.dueDate != null){
                    LocalDate date = LocalDate.parse(dto.dueDate);

                    LocalTime time = LocalTime.parse(dto.time, timeFormatter);
                    LocalDateTime dateTime = LocalDateTime.of(date, time);

                    QuestExecution execution = new QuestExecution(dateTime, QuestStatus.ACTIVE, (int)questId);
                    questDao.insertExecution(execution);
                }
                else if(dto.isRepeating && dto.startDate != null && dto.endDate != null){
                    LocalDate start = LocalDate.parse(dto.startDate);
                    LocalDate end = LocalDate.parse(dto.endDate);

                    LocalTime time = LocalTime.parse(dto.time, timeFormatter);

                    LocalDateTime current = LocalDateTime.of(start, time);
                    LocalDateTime endTime = LocalDateTime.of(end, time);

                    while (!current.isAfter(endTime)) {
                        QuestExecution execution = new QuestExecution(current, QuestStatus.ACTIVE, (int)questId);
                        questDao.insertExecution(execution);

                        if (dto.unit == RepeatingUnit.DAYS) {
                            current = current.plusDays(dto.repeatingInterval);
                        } else if (dto.unit == RepeatingUnit.WEEKS) {
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

    public LiveData<Result<String>> updateQuest(int executionId, AddQuestDto dto){
        MutableLiveData<Result<String>> result = new MutableLiveData<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                QuestWithExecutions questExec = questDao.getQuestWithSingleExecutionSync(executionId);
                Quest quest = questExec.quest;

                quest.setName(dto.name);
                quest.setDescription(dto.description);
                quest.setDifficulty(dto.difficulty);
                quest.setImportance(dto.importance);

                if(questDao.updateQuest(quest) != 1){
                    throw new Exception("quest update failed");
                }

                for(QuestExecution exec: questExec.executions){
                    QuestStatus status = exec.getStatus();
                    if((status == QuestStatus.ACTIVE || status == QuestStatus.PAUSED)
                            && exec.getDate().isAfter(LocalDateTime.now())){

                        LocalDateTime originalDate = exec.getDate();

                        LocalTime newTime = LocalTime.parse(dto.time, DateTimeFormatter.ofPattern("HH:mm"));
                        LocalDateTime newDateTime = originalDate.withHour(newTime.getHour())
                                .withMinute(newTime.getMinute())
                                .withSecond(0)
                                .withNano(0);

                        exec.setDate(newDateTime);

                        questDao.updateExecution(exec);
                    }
                }

                result.postValue(Result.success("Quest updated successfully"));

            } catch (Exception e) {
                result.postValue(Result.error("Error updating quest: " + e.getMessage()));
            }
        });

        return result;
    }

    public LiveData<List<DetailedQuestExecutionDto>> getDetailedExecutionsForUser(String userId, boolean timeFilter) {
        return Transformations.map(
                questDao.getAllQuestsWithExecutionsForUser(userId),
                questList -> {
                    List<DetailedQuestExecutionDto> detailedList = new ArrayList<>();
                    for (QuestWithExecutions questWithExec : questList) {
                        for (QuestExecution exec : questWithExec.executions) {
                            if (!timeFilter || exec.getDate().isAfter(LocalDateTime.now())) {
                                detailedList.add(
                                        new DetailedQuestExecutionDto(
                                                questWithExec.quest,
                                                exec,
                                                questWithExec.category
                                        )
                                );
                            }
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

    // quest must be active
    public LiveData<Result<String>> changeActiveQuestStatus(int questId, int executionId, QuestStatus status){
        MutableLiveData<Result<String>> result = new MutableLiveData<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                QuestExecution execution = questDao.getExecutionByIdSync(executionId);
                QuestWithExecutions questExec = questDao.getQuestWithExecutions(questId);
                Quest quest = questExec.quest;

                if(execution.getStatus() != QuestStatus.ACTIVE){
                    throw new Exception("Quest isnt active!");
                }

                if(status == QuestStatus.COMPLETED){
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime scheduled = execution.getDate();

                    LocalDateTime earliestAllowed = scheduled.minusDays(3);
                    LocalDateTime latestAllowed = scheduled;

                    // ovaj if otkomentarsiati pre odbrane, za potrebe testiranja
                    // zgodno da se mogu resavati taskovi u buducnost
//                    if (now.isBefore(earliestAllowed) || !now.isBefore(latestAllowed)) {
//                        result.postValue(Result.error("Quest can be completed only 3 days before the scheduled date!"));
//                        return;
//                    }

                    execution.setStatus(status);
                    execution.setQuestCompleted(LocalDateTime.now());

                    // upit koliko je u danu vec reseno tog diff/imp itd
                    boolean xpForDifficulty = canGetXpForDifficulty(quest.getDifficulty(), quest.getUserId());
                    boolean xpForImportance = canGetXpForImportance(quest.getImportance(), quest.getUserId());

                    if (!xpForDifficulty || !xpForImportance)
                        execution.setQuotaExceeded(true);

                    // dodati xp korisniku i procitati iz questa koliko dobija
                    userService.completeQuest(quest, xpForDifficulty,xpForImportance, pair -> {
                        System.out.println("User earned: " + pair.first + " XP - " + pair.second);
                        execution.setXpEarned(pair.first);

                        if (pair.second) {
                            bossService.handleBossAfterLevelUp(quest.getUserId());
                        }
                    });

                    questDao.updateExecution(execution);
                    result.postValue(Result.success("Quest completed successfully"));
                }
                else if(status == QuestStatus.CANCELLED){
                    execution.setStatus(status);
                    questDao.updateExecution(execution);

                    result.postValue(Result.success("Quest cancelled successfully"));
                }
                else if(status == QuestStatus.PAUSED){
                    for(QuestExecution exec: questExec.executions){
                        if(exec.getStatus() == QuestStatus.ACTIVE){
                            exec.setStatus(status);
                            questDao.updateExecution(exec);
                        }
                    }
                    result.postValue(Result.success("Quests paused successfully"));
                }
            } catch (Exception e) {
                Log.d("ERRORCINA", e.toString());
                result.postValue(Result.error("Error changing quest status: " + e.getMessage()));
            }
        });

        return result;
    }

    private boolean canGetXpForImportance(Importance importance, String userId) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        int importanceOccurrences;

        switch (importance) {
            case LOW:
            case MEDIUM:
            case HIGH: {
                startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
                endDate = startDate.plusDays(1).minusNanos(1);

                importanceOccurrences = questDao.countQuestExecutionsForUserByImportance(
                        userId, importance.toString(), startDate, endDate
                );

                if ((importance == Importance.LOW || importance == Importance.MEDIUM) && importanceOccurrences > 5) {
                    return false;
                }
                if (importance == Importance.HIGH && importanceOccurrences > 2) {
                    return false;
                }
                break;
            }
            case SPECIAL: {
                LocalDate today = LocalDate.now();
                startDate = today.withDayOfMonth(1).atStartOfDay();
                endDate = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

                importanceOccurrences = questDao.countQuestExecutionsForUserByImportance(
                        userId, importance.toString(), startDate, endDate
                );

                if (importanceOccurrences > 1) {
                    return false;
                }
                break;
            }
        }

        return true;
    }

    private boolean canGetXpForDifficulty(Difficulty difficulty, String userId) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        int difficultyOccurrences;

        switch (difficulty) {
            case NOVICE:
            case ADVENTURER:
            case VETERAN: {
                startDate = LocalDate.now().atStartOfDay();
                endDate = startDate.plusDays(1).minusNanos(1);

                difficultyOccurrences = questDao.countQuestExecutionsForUserByDifficulty(
                        userId, difficulty.toString(), startDate, endDate
                );

                if ((difficulty == Difficulty.NOVICE || difficulty == Difficulty.ADVENTURER) && difficultyOccurrences > 5) {
                    return false;
                }
                if (difficulty == Difficulty.VETERAN && difficultyOccurrences > 2) {
                    return false;
                }
                break;
            }
            case LEGENDARY: {
                LocalDate today = LocalDate.now();

                // Start of week (Monday at 00:00)
                LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
                startDate = startOfWeek.atStartOfDay();  // ✅ LocalDateTime

                // End of week (Sunday at 23:59:59.999...)
                LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY).plusDays(1);
                endDate = endOfWeek.atTime(LocalTime.MAX);  // ✅ LocalDateTime

                System.out.println("Start:  " + startDate.toString() + " End: " + endDate.toString() );

                difficultyOccurrences = questDao.countQuestExecutionsForUserByDifficulty(
                        userId, difficulty.toString(), startDate, endDate
                );

                if (difficultyOccurrences > 1) {
                    return false;
                }
                break;
            }
        }

        return true;
    }

    public LiveData<Result<String>> unpauseQuest(int questId){
        MutableLiveData<Result<String>> result = new MutableLiveData<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                QuestWithExecutions questExec = questDao.getQuestWithExecutions(questId);

                for(QuestExecution exec: questExec.executions){
                    if(exec.getStatus() == QuestStatus.PAUSED){
                        exec.setStatus(QuestStatus.ACTIVE);
                        questDao.updateExecution(exec);
                    }
                }

                result.postValue(Result.success("Quests unpaused successfully"));
            } catch (Exception e) {
                result.postValue(Result.error("Error changing quest status: " + e.getMessage()));
            }
        });

        return result;
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public void startAutoFailTask() {
        Runnable task = () -> {
            long nowMillis = System.currentTimeMillis();
            try {
                List<QuestExecution> overdueExecutions = questDao.getActiveExecutionsBefore(nowMillis, QuestStatus.ACTIVE);
                for (QuestExecution exec : overdueExecutions) {
                    exec.setStatus(QuestStatus.FAILED);
                    questDao.updateExecution(exec);
                }
            } catch (Exception e) {
                Log.e("QuestRepo", "Error auto-failing quests: " + e.getMessage());
            }
        };

        scheduler.scheduleWithFixedDelay(task, 0, 5, TimeUnit.MINUTES);
    }

}