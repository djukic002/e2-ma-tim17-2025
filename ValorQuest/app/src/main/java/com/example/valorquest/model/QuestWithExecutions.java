package com.example.valorquest.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class QuestWithExecutions {
    @Embedded
    public Quest quest;

    @Relation(
            parentColumn = "id",
            entityColumn = "questId"
    )
    public List<QuestExecution> executions;

    @Relation(
            parentColumn = "categoryId",
            entityColumn = "id"
    )
    public Category category;
}
