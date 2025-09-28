package com.example.valorquest.utils;

import android.util.Pair;

@FunctionalInterface
public interface QuestResultListener<T> {
    void onResult(Pair<Integer, Boolean> result);
}
