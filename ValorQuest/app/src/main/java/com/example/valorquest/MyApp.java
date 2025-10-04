package com.example.valorquest;

import android.app.Application;
import android.util.Log;

import com.example.valorquest.service.AllianceMissionService;
import com.example.valorquest.service.QuestService;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyApp extends Application {
    @Inject
    AllianceMissionService allianceMissionService;

    @Inject
    QuestService questService;

    @Override
    public void onCreate() {
        super.onCreate();

        allianceMissionService.startAutoFailTask();
        questService.startAutoFailTask();
    }
}
