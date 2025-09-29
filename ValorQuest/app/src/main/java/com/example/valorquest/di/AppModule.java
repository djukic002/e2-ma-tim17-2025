package com.example.valorquest.di;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.data.local.CategoryDao;
import com.example.valorquest.data.local.QuestDao;

import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "valorquest.db")
                .setQueryCallback((sqlQuery, bindArgs) -> {
                    // Log.d("RoomQuery", "SQL: " + sqlQuery + " | Args: " + bindArgs);
                }, Executors.newSingleThreadExecutor())
                .fallbackToDestructiveMigration()
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .build();

        // Clear all tables on startup (for testing/dev only)
        // AppDatabase.databaseWriteExecutor.execute(db::clearAllData);

        return db;
    }

    @Provides
    public CategoryDao provideCategoryDao(AppDatabase db) {
        return db.categoryDao();
    }
    @Provides
    public QuestDao provideQuestDao(AppDatabase db) {
        return db.questDao();
    }
}