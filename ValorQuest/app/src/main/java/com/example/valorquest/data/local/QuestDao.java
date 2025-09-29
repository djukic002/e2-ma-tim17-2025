package com.example.valorquest.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.valorquest.model.Quest;
import com.example.valorquest.model.QuestExecution;
import com.example.valorquest.model.QuestWithExecutions;
import com.example.valorquest.model.enums.QuestStatus;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface QuestDao {
    @Insert
    long insertQuest(Quest quest); // generated id

    @Insert
    void insertExecution(QuestExecution execution);

    @Transaction
    default void insertQuestWithExecutions(QuestWithExecutions questWithExecutions) {
        long questId = insertQuest(questWithExecutions.quest);

        for (QuestExecution execution : questWithExecutions.executions) {
            execution.setQuestId((int) questId);
            insertExecution(execution);
        }
    }

    @Transaction
    @Query("SELECT * FROM quests WHERE id = :questId")
    QuestWithExecutions getQuestWithExecutions(int questId);

    @Transaction
    @Query("SELECT * FROM quests")
    LiveData<List<QuestWithExecutions>> getAllQuestsWithExecutions();

    @Transaction
    @Query("SELECT * FROM quests " +
            "WHERE userId = :userId " +
            "ORDER BY (SELECT MIN(date) " +
            "          FROM quest_executions " +
            "          WHERE quest_executions.questId = quests.id) ASC")
    LiveData<List<QuestWithExecutions>> getAllQuestsWithExecutionsForUser(String userId);

    @Query("SELECT * FROM quest_executions WHERE id = :executionId")
    QuestExecution getExecutionByIdSync(int executionId);
    @Query("SELECT * FROM quests WHERE id = :questId")
    Quest getQuestByIdSync(int questId);

    @Transaction
    @Query("SELECT * FROM quests WHERE id = (SELECT questId FROM quest_executions WHERE id = :executionId)")
    LiveData<QuestWithExecutions> getQuestWithSingleExecution(int executionId);

    @Transaction
    @Query("SELECT * FROM quests WHERE id = (SELECT questId FROM quest_executions WHERE id = :executionId)")
    QuestWithExecutions getQuestWithSingleExecutionSync(int executionId);

    @Delete
    void deleteExecution(QuestExecution execution);

    @Delete
    void deleteQuest(Quest quest);

    @Update
    int updateQuest(Quest quest);
    @Update
    int updateExecution(QuestExecution execution);

    @Query("SELECT * FROM quest_executions WHERE status = :status AND date < :now")
    List<QuestExecution> getActiveExecutionsBefore(long now, QuestStatus status);

    @Query("SELECT COUNT(qe.id) FROM quest_executions qe " +
            "INNER JOIN quests q ON q.id = qe.questId " +
            "WHERE q.userId = :userId AND q.difficulty = :difficulty AND " +
            "qe.questCompleted BETWEEN :startDateTime AND :endDateTime" +
            " AND qe.status = :completedStatus ")
    int countQuestExecutionsForUserByDifficulty(
            String userId,
            String difficulty,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            QuestStatus completedStatus
    );

    @Query("SELECT COUNT(qe.id) FROM quest_executions qe " +
            "INNER JOIN quests q ON q.id = qe.questId " +
            "WHERE q.userId = :userId AND q.importance = :importance AND " +
            "qe.questCompleted BETWEEN :startDateTime AND :endDateTime " +
            "AND qe.status = :completedStatus ")
    int countQuestExecutionsForUserByImportance(
            String userId,
            String importance,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            QuestStatus completedStatus
    );

    @Query("SELECT qe.* FROM quest_executions qe " +
            "INNER JOIN quests q ON q.id = qe.questId " +
            "WHERE q.userId = :userId " +
            "AND qe.createdInLevel = :level " +
            "AND qe.quotaExceeded = 0 " +
            "AND qe.status not in ('PAUSED','CANCELLED')")
    List<QuestExecution> getCreatedExecByLevelAndStatusWithoutQuotaExceeding(String userId, int level);

    @Query("SELECT qe.* FROM quest_executions qe " +
            "INNER JOIN quests q ON q.id = qe.questId " +
            "WHERE q.userId = :userId " +
            "AND qe.quotaExceeded = 0 " +
            "AND qe.status = :completedStatus " +
            "AND qe.completedInLevel = :level")
    List<QuestExecution> getCompletedForLevelWithoutQuotaExceeding(String userId, int level,QuestStatus completedStatus);

    @Query("SELECT qe.* FROM quest_executions qe " +
            "INNER JOIN quests q ON q.id = qe.questId " +
            "WHERE q.userId = :userId " +
            "AND qe.quotaExceeded = 0 " +
            "AND qe.status = :completedStatus " +
            "AND qe.completedInLevel = :level " +
            "AND qe.createdInLevel < :level")
    List<QuestExecution> getOldCompletedForLevelWithoutQuotaExceeding(String userId, int level,QuestStatus completedStatus);
}