package com.example.valorquest.utils;

import androidx.room.TypeConverter;

import com.example.valorquest.model.enums.Difficulty;
import com.example.valorquest.model.enums.Importance;
import com.example.valorquest.model.enums.QuestStatus;
import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Converters {
    // === LocalDateTime ===
    @TypeConverter
    public static Long fromLocalDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null :
                dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @TypeConverter
    public static LocalDateTime toLocalDateTime(Long millis) {
        return millis == null ? null :
                LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    // === LocalDate ===
    @TypeConverter
    public static Long fromLocalDate(LocalDate date) {
        return date == null ? null :
                date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @TypeConverter
    public static LocalDate toLocalDate(Long millis) {
        return millis == null ? null :
                LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    // === Enums (Difficulty, Importance, QuestStatus) ===
    @TypeConverter
    public static String fromDifficulty(Difficulty difficulty) {
        return difficulty == null ? null : difficulty.name();
    }

    @TypeConverter
    public static Difficulty toDifficulty(String value) {
        return value == null ? null : Difficulty.valueOf(value);
    }

    @TypeConverter
    public static String fromImportance(Importance importance) {
        return importance == null ? null : importance.name();
    }

    @TypeConverter
    public static Importance toImportance(String value) {
        return value == null ? null : Importance.valueOf(value);
    }

    @TypeConverter
    public static String fromQuestStatus(QuestStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static QuestStatus toQuestStatus(String value) {
        return value == null ? null : QuestStatus.valueOf(value);
    }

    public static LocalDateTime fromFirebaseTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        Instant instant = timestamp.toDate().toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
