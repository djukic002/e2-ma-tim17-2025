package com.example.valorquest.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.valorquest.model.Category;

@Database(entities = {Category.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CategoryDao categoryDao();
}