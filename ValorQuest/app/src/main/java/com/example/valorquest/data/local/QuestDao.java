package com.example.valorquest.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

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
    @Query("SELECT * FROM quests WHERE id = :questId")
    QuestWithExecutions getQuestWithExecutions(int questId);

    @Transaction
    @Query("SELECT * FROM quests")
    List<QuestWithExecutions> getAllQuestsWithExecutions();
}