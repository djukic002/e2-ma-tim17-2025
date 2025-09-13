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
    LiveData<QuestExecution> getExecutionById(int executionId);
    @Query("SELECT * FROM quests WHERE id = :questId")
    LiveData<Quest> getQuestById(int questId);

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
}