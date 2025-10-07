package com.example.valorquest.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.local.AppDatabase;
import com.example.valorquest.model.dto.CategoryQuestCountDTO;
import com.example.valorquest.model.dto.DailyXpDTO;
import com.example.valorquest.model.enums.QuestStatus;
import com.example.valorquest.service.QuestService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {
    private final QuestService questService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    // Quest status counts (for donut chart)
    private final MutableLiveData<Integer> completedCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> failedCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> activeCount = new MutableLiveData<>(0);
    
    // Consecutive days streak
    private final MutableLiveData<Integer> consecutiveDays = new MutableLiveData<>(0);
    
    // Category quest counts (for bar chart)
    private final MutableLiveData<List<CategoryQuestCountDTO>> categoryCounts = new MutableLiveData<>();
    
    // Daily XP for last 7 days (for line chart)
    private final MutableLiveData<List<DailyXpDTO>> dailyXpList = new MutableLiveData<>();
    
    // Most completed difficulty
    private final MutableLiveData<String> mostCompletedDifficulty = new MutableLiveData<>("");
    
    // Loading and error states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    @Inject
    public StatisticsViewModel(QuestService questService) {
        this.questService = questService;
    }

    public void loadStatistics() {
        String userId = getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e("StatisticsViewModel", "No user logged in");
            errorMessage.setValue("No user logged in");
            return;
        }
        
        Log.d("StatisticsViewModel", "Loading statistics for user: " + userId);
        isLoading.setValue(true);
        
        // Load all statistics in background thread
        executor.execute(() -> {
            try {
                Log.d("StatisticsViewModel", "Starting statistics loading...");
                
                // Load quest status counts
                loadQuestStatusCounts(userId);
                
                // Load consecutive days
                loadConsecutiveDays(userId);
                
                // Load category counts
                loadCategoryCounts(userId);
                
                // Load daily XP
                loadDailyXp(userId);
                
                // Load most completed difficulty
                loadMostCompletedDifficulty(userId);
                
                Log.d("StatisticsViewModel", "Statistics loading completed");
                isLoading.postValue(false);
                
            } catch (Exception e) {
                Log.e("StatisticsViewModel", "Error loading statistics: " + e.getMessage(), e);
                errorMessage.postValue("Failed to load statistics: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    private void loadQuestStatusCounts(String userId) {
        try {
            Log.d("StatisticsViewModel", "Loading quest status counts for user: " + userId);
            
            int completed = questService.getQuestExecutionCountByStatus(userId, QuestStatus.COMPLETED);
            int failed = questService.getQuestExecutionCountByStatus(userId, QuestStatus.FAILED);
            int active = questService.getQuestExecutionCountByStatus(userId, QuestStatus.ACTIVE);
            
            Log.d("StatisticsViewModel", "Quest status counts - Completed: " + completed + ", Failed: " + failed + ", Active: " + active);
            
            completedCount.postValue(completed);
            failedCount.postValue(failed);
            activeCount.postValue(active);
        } catch (Exception e) {
            Log.e("StatisticsViewModel", "Failed to load quest status counts: " + e.getMessage(), e);
            errorMessage.postValue("Failed to load quest status counts: " + e.getMessage());
        }
    }
    
    private void loadConsecutiveDays(String userId) {
        try {
            Log.d("StatisticsViewModel", "Loading consecutive days for user: " + userId);
            int streak = questService.getConsecutiveCompletedDays(userId);
            Log.d("StatisticsViewModel", "Consecutive days streak: " + streak);
            consecutiveDays.postValue(streak);
        } catch (Exception e) {
            Log.e("StatisticsViewModel", "Failed to load consecutive days: " + e.getMessage(), e);
            errorMessage.postValue("Failed to load consecutive days: " + e.getMessage());
        }
    }
    
    private void loadCategoryCounts(String userId) {
        try {
            Log.d("StatisticsViewModel", "Loading category counts for user: " + userId);
            List<CategoryQuestCountDTO> counts = questService.getCompletedQuestCountByCategory(userId);
            Log.d("StatisticsViewModel", "Category counts loaded: " + (counts != null ? counts.size() : 0) + " categories");
            if (counts != null) {
                for (CategoryQuestCountDTO category : counts) {
                    Log.d("StatisticsViewModel", "Category: " + category.getCategoryName() + " - Count: " + category.getCount());
                }
            } else {
                Log.w("StatisticsViewModel", "Category counts is null!");
            }
            Log.d("StatisticsViewModel", "Posting category counts to LiveData: " + (counts != null ? counts.size() : 0) + " items");
            categoryCounts.postValue(counts);
            Log.d("StatisticsViewModel", "Category counts posted successfully");
        } catch (Exception e) {
            Log.e("StatisticsViewModel", "Failed to load category counts: " + e.getMessage(), e);
            errorMessage.postValue("Failed to load category counts: " + e.getMessage());
        }
    }
    
    private void loadDailyXp(String userId) {
        try {
            Log.d("StatisticsViewModel", "Loading daily XP for user: " + userId);
            List<DailyXpDTO> xpList = questService.getLast7DaysXp(userId);
            Log.d("StatisticsViewModel", "Daily XP loaded: " + (xpList != null ? xpList.size() : 0) + " days");
            if (xpList != null) {
                int totalXp = 0;
                for (DailyXpDTO dailyXp : xpList) {
                    Log.d("StatisticsViewModel", "Date: " + dailyXp.getDate() + " - XP: " + dailyXp.getXp());
                    totalXp += dailyXp.getXp();
                }
                Log.d("StatisticsViewModel", "Total XP over 7 days: " + totalXp);
            } else {
                Log.w("StatisticsViewModel", "Daily XP list is null!");
            }
            Log.d("StatisticsViewModel", "Posting daily XP to LiveData: " + (xpList != null ? xpList.size() : 0) + " items");
            dailyXpList.postValue(xpList);
            Log.d("StatisticsViewModel", "Daily XP posted successfully");
        } catch (Exception e) {
            Log.e("StatisticsViewModel", "Failed to load daily XP: " + e.getMessage(), e);
            errorMessage.postValue("Failed to load daily XP: " + e.getMessage());
        }
    }
    
    private void loadMostCompletedDifficulty(String userId) {
        try {
            Log.d("StatisticsViewModel", "Loading most completed difficulty for user: " + userId);
            String difficulty = questService.getMostCompletedDifficulty(userId);
            Log.d("StatisticsViewModel", "Most completed difficulty: " + (difficulty != null ? difficulty : "None"));
            mostCompletedDifficulty.postValue(difficulty != null ? difficulty : "None");
        } catch (Exception e) {
            Log.e("StatisticsViewModel", "Failed to load most completed difficulty: " + e.getMessage(), e);
            errorMessage.postValue("Failed to load most completed difficulty: " + e.getMessage());
        }
    }
    
    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    // Getters
    public LiveData<Integer> getCompletedCount() { return completedCount; }
    public LiveData<Integer> getFailedCount() { return failedCount; }
    public LiveData<Integer> getActiveCount() { return activeCount; }
    public LiveData<Integer> getConsecutiveDays() { return consecutiveDays; }
    public LiveData<List<CategoryQuestCountDTO>> getCategoryCounts() { return categoryCounts; }
    public LiveData<List<DailyXpDTO>> getDailyXpList() { return dailyXpList; }
    public LiveData<String> getMostCompletedDifficulty() { return mostCompletedDifficulty; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
