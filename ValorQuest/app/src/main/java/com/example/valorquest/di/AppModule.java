package com.example.valorquest.di;

import android.content.Context;

import androidx.room.Room;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.data.local.CategoryDao;

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
        return Room.databaseBuilder(context, AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public CategoryDao provideCategoryDao(AppDatabase db) {
        return db.categoryDao();
    }
}