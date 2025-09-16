package com.example.valorquest.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.valorquest.model.Category;
import com.example.valorquest.model.Quest;
import com.example.valorquest.model.QuestExecution;
import com.example.valorquest.utils.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Category.class,
        Quest.class,
        QuestExecution.class},
        version = 3,
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract CategoryDao categoryDao();
    public abstract QuestDao questDao();
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
}