package com.example.valorquest.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.CategoryQuestCountDTO;
import com.example.valorquest.model.dto.DailyXpDTO;
import com.example.valorquest.viewmodel.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatisticsFragment extends Fragment {
    private StatisticsViewModel viewModel;
    
    // Chart components
    private PieChart pieChartQuestStatus;
    private BarChart barChartCategories;
    private LineChart lineChartDailyXp;
    
    // Quest status views
    private TextView tvCompletedCount;
    private TextView tvFailedCount;
    private TextView tvActiveCount;
    
    // Consecutive days
    private TextView tvConsecutiveDays;
    
    // Most completed difficulty
    private TextView tvMostCompletedDifficulty;
    
    // Category stats
    private TextView tvCategoryStats;
    
    // Daily XP stats
    private TextView tvDailyXpStats;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        
        Log.d("StatisticsFragment", "ViewModel initialized: " + (viewModel != null));
        
        initializeViews(view);
        observeViewModel();
        
        // Load statistics
        Log.d("StatisticsFragment", "Starting to load statistics...");
        viewModel.loadStatistics();
    }

    private void initializeViews(View view) {
        // Chart components
        pieChartQuestStatus = view.findViewById(R.id.pie_chart_quest_status);
        barChartCategories = view.findViewById(R.id.bar_chart_categories);
        lineChartDailyXp = view.findViewById(R.id.line_chart_daily_xp);
        
        // Quest status counts
        tvCompletedCount = view.findViewById(R.id.tv_completed_count);
        tvFailedCount = view.findViewById(R.id.tv_failed_count);
        tvActiveCount = view.findViewById(R.id.tv_active_count);
        
        // Consecutive days
        tvConsecutiveDays = view.findViewById(R.id.tv_consecutive_days);
        
        // Most completed difficulty
        tvMostCompletedDifficulty = view.findViewById(R.id.tv_most_completed_difficulty);
        
        // Category stats
        tvCategoryStats = view.findViewById(R.id.tv_category_stats);
        
        // Daily XP stats
        tvDailyXpStats = view.findViewById(R.id.tv_daily_xp_stats);
        
        // Initialize charts
        setupPieChart();
        setupBarChart();
        setupLineChart();
    }

    private void observeViewModel() {
        Log.d("StatisticsFragment", "Setting up observers");
        
        // Check current values in LiveData
        Log.d("StatisticsFragment", "Current category counts: " + (viewModel.getCategoryCounts().getValue() != null ? viewModel.getCategoryCounts().getValue().size() : "null"));
        Log.d("StatisticsFragment", "Current daily XP: " + (viewModel.getDailyXpList().getValue() != null ? viewModel.getDailyXpList().getValue().size() : "null"));
        
        // Quest status counts - update both text and pie chart
        viewModel.getCompletedCount().observe(getViewLifecycleOwner(), count -> {
            Log.d("StatisticsFragment", "Completed count updated: " + count);
            tvCompletedCount.setText(String.valueOf(count));
            updatePieChart();
        });
        
        viewModel.getFailedCount().observe(getViewLifecycleOwner(), count -> {
            Log.d("StatisticsFragment", "Failed count updated: " + count);
            tvFailedCount.setText(String.valueOf(count));
            updatePieChart();
        });
        
        viewModel.getActiveCount().observe(getViewLifecycleOwner(), count -> {
            Log.d("StatisticsFragment", "Active count updated: " + count);
            tvActiveCount.setText(String.valueOf(count));
            updatePieChart();
        });
        
        // Consecutive days
        viewModel.getConsecutiveDays().observe(getViewLifecycleOwner(), days -> {
            Log.d("StatisticsFragment", "Consecutive days updated: " + days);
            tvConsecutiveDays.setText(days + " days");
        });
        
        // Most completed difficulty
        viewModel.getMostCompletedDifficulty().observe(getViewLifecycleOwner(), difficulty -> {
            Log.d("StatisticsFragment", "Most completed difficulty updated: " + difficulty);
            tvMostCompletedDifficulty.setText(difficulty);
        });
        
        // Category counts - update both text and bar chart
        viewModel.getCategoryCounts().observe(getViewLifecycleOwner(), categoryCounts -> {
            Log.d("StatisticsFragment", "Category counts observer triggered: " + (categoryCounts != null ? categoryCounts.size() : 0) + " items");
            if (categoryCounts != null) {
                Log.d("StatisticsFragment", "Category counts data received:");
                for (int i = 0; i < categoryCounts.size(); i++) {
                    Log.d("StatisticsFragment", "  [" + i + "] " + categoryCounts.get(i).getCategoryName() + " = " + categoryCounts.get(i).getCount());
                }
            } else {
                Log.w("StatisticsFragment", "Category counts is null in observer!");
            }
            updateCategoryStats(categoryCounts);
            updateBarChart(categoryCounts);
        });
        
        // Daily XP - update both text and line chart
        viewModel.getDailyXpList().observe(getViewLifecycleOwner(), dailyXpList -> {
            Log.d("StatisticsFragment", "Daily XP observer triggered: " + (dailyXpList != null ? dailyXpList.size() : 0) + " items");
            if (dailyXpList != null) {
                Log.d("StatisticsFragment", "Daily XP data received:");
                for (int i = 0; i < dailyXpList.size(); i++) {
                    Log.d("StatisticsFragment", "  [" + i + "] " + dailyXpList.get(i).getDate() + " = " + dailyXpList.get(i).getXp() + " XP");
                }
            } else {
                Log.w("StatisticsFragment", "Daily XP list is null in observer!");
            }
            updateDailyXpStats(dailyXpList);
            updateLineChart(dailyXpList);
        });
        
        // Error handling
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e("StatisticsFragment", "Error: " + errorMessage);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateCategoryStats(List<CategoryQuestCountDTO> categoryCounts) {
        Log.d("StatisticsFragment", "Category stats updated: " + (categoryCounts != null ? categoryCounts.size() : 0) + " categories");
        
        if (categoryCounts == null || categoryCounts.isEmpty()) {
            tvCategoryStats.setText("No completed quests by category");
            return;
        }
        
        StringBuilder stats = new StringBuilder();
        for (CategoryQuestCountDTO category : categoryCounts) {
            Log.d("StatisticsFragment", "Category: " + category.getCategoryName() + " - Count: " + category.getCount());
            stats.append(category.getCategoryName())
                 .append(": ")
                 .append(category.getCount())
                 .append(" quests\n");
        }
        tvCategoryStats.setText(stats.toString().trim());
    }
    
    private void updateDailyXpStats(List<DailyXpDTO> dailyXpList) {
        Log.d("StatisticsFragment", "Daily XP stats updated: " + (dailyXpList != null ? dailyXpList.size() : 0) + " days");
        
        if (dailyXpList == null || dailyXpList.isEmpty()) {
            tvDailyXpStats.setText("No XP data for last 7 days");
            return;
        }
        
        StringBuilder stats = new StringBuilder();
        int totalXp = 0;
        for (DailyXpDTO dailyXp : dailyXpList) {
            Log.d("StatisticsFragment", "Daily XP: " + dailyXp.getDate() + " - " + dailyXp.getXp() + " XP");
            String dayName = dailyXp.getDate().getDayOfWeek().toString();
            dayName = dayName.substring(0, 1) + dayName.substring(1).toLowerCase();
            stats.append(dayName)
                 .append(": ")
                 .append(dailyXp.getXp())
                 .append(" XP\n");
            totalXp += dailyXp.getXp();
        }
        stats.append("\nTotal: ").append(totalXp).append(" XP");
        Log.d("StatisticsFragment", "Total XP over 7 days: " + totalXp);
        tvDailyXpStats.setText(stats.toString());
    }
    
    // Chart setup methods
    private void setupPieChart() {
        pieChartQuestStatus.setUsePercentValues(false);
        pieChartQuestStatus.getDescription().setEnabled(false);
        pieChartQuestStatus.setExtraOffsets(5, 10, 5, 5);
        pieChartQuestStatus.setDragDecelerationFrictionCoef(0.95f);
        pieChartQuestStatus.setDrawHoleEnabled(true);
        pieChartQuestStatus.setHoleColor(Color.TRANSPARENT);
        pieChartQuestStatus.setTransparentCircleColor(Color.WHITE);
        pieChartQuestStatus.setTransparentCircleAlpha(110);
        pieChartQuestStatus.setHoleRadius(58f);
        pieChartQuestStatus.setTransparentCircleRadius(61f);
        pieChartQuestStatus.setDrawCenterText(true);
        pieChartQuestStatus.setCenterText("Quest Status");
        pieChartQuestStatus.setCenterTextSize(16f);
        pieChartQuestStatus.setCenterTextColor(Color.BLACK);
        pieChartQuestStatus.setRotationAngle(0);
        pieChartQuestStatus.setRotationEnabled(true);
        pieChartQuestStatus.setHighlightPerTapEnabled(true);
        pieChartQuestStatus.animateY(1400);
    }
    
    private void setupBarChart() {
        Log.d("StatisticsFragment", "Setting up bar chart");
        barChartCategories.getDescription().setEnabled(false);
        barChartCategories.setDrawGridBackground(false);
        barChartCategories.setDrawBarShadow(false);
        barChartCategories.setDrawValueAboveBar(true);
        barChartCategories.setMaxVisibleValueCount(10);
        barChartCategories.setPinchZoom(false);
        barChartCategories.setDrawGridBackground(false);
        barChartCategories.setTouchEnabled(true);
        barChartCategories.setDragEnabled(true);
        barChartCategories.setScaleEnabled(true);
        barChartCategories.animateY(1500);
        
        XAxis xAxis = barChartCategories.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5);
        xAxis.setGranularityEnabled(true);
        
        barChartCategories.getAxisLeft().setAxisMinimum(0f);
        barChartCategories.getAxisRight().setEnabled(false);
        barChartCategories.getLegend().setEnabled(false);
    }
    
    private void setupLineChart() {
        Log.d("StatisticsFragment", "Setting up line chart");
        lineChartDailyXp.getDescription().setEnabled(false);
        lineChartDailyXp.setTouchEnabled(true);
        lineChartDailyXp.setDragEnabled(true);
        lineChartDailyXp.setScaleEnabled(true);
        lineChartDailyXp.setPinchZoom(true);
        lineChartDailyXp.setBackgroundColor(Color.WHITE);
        lineChartDailyXp.animateX(1500);
        
        XAxis xAxis = lineChartDailyXp.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        
        lineChartDailyXp.getAxisLeft().setAxisMinimum(0f);
        lineChartDailyXp.getAxisRight().setEnabled(false);
        lineChartDailyXp.getLegend().setEnabled(false);
    }
    
    // Chart update methods
    private void updatePieChart() {
        Integer completed = viewModel.getCompletedCount().getValue();
        Integer failed = viewModel.getFailedCount().getValue();
        Integer active = viewModel.getActiveCount().getValue();
        
        if (completed == null) completed = 0;
        if (failed == null) failed = 0;
        if (active == null) active = 0;
        
        List<PieEntry> entries = new ArrayList<>();
        if (completed > 0) entries.add(new PieEntry(completed, "Completed"));
        if (failed > 0) entries.add(new PieEntry(failed, "Failed"));
        if (active > 0) entries.add(new PieEntry(active, "Active"));
        
        if (entries.isEmpty()) {
            pieChartQuestStatus.setData(null);
            pieChartQuestStatus.invalidate();
            return;
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        
        // Colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#C9A227")); // Gold for completed
        colors.add(Color.parseColor("#7A0F0F")); // Red for failed
        colors.add(Color.parseColor("#4A90E2")); // Blue for active
        dataSet.setColors(colors);
        
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);
        
        pieChartQuestStatus.setData(data);
        pieChartQuestStatus.invalidate();
    }
    
    private void updateBarChart(List<CategoryQuestCountDTO> categoryCounts) {
        Log.d("StatisticsFragment", "Updating bar chart with " + (categoryCounts != null ? categoryCounts.size() : 0) + " categories");
        
        if (categoryCounts == null || categoryCounts.isEmpty()) {
            Log.d("StatisticsFragment", "No category data, clearing bar chart");
            barChartCategories.setData(null);
            barChartCategories.invalidate();
            return;
        }
        
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < categoryCounts.size(); i++) {
            CategoryQuestCountDTO category = categoryCounts.get(i);
            Log.d("StatisticsFragment", "Bar chart entry " + i + ": " + category.getCategoryName() + " = " + category.getCount());
            entries.add(new BarEntry(i, category.getCount()));
            labels.add(category.getCategoryName());
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Quests by Category");
        dataSet.setColors(Color.parseColor("#C9A227"));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);
        barChartCategories.setData(data);
        
        XAxis xAxis = barChartCategories.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        
        barChartCategories.getAxisLeft().setAxisMinimum(0f);
        barChartCategories.getAxisRight().setEnabled(false);
        
        Log.d("StatisticsFragment", "Bar chart updated with " + entries.size() + " entries");
        barChartCategories.invalidate();
    }
    
    private void updateLineChart(List<DailyXpDTO> dailyXpList) {
        Log.d("StatisticsFragment", "Updating line chart with " + (dailyXpList != null ? dailyXpList.size() : 0) + " days");
        
        if (dailyXpList == null || dailyXpList.isEmpty()) {
            Log.d("StatisticsFragment", "No daily XP data, clearing line chart");
            lineChartDailyXp.setData(null);
            lineChartDailyXp.invalidate();
            return;
        }
        
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < dailyXpList.size(); i++) {
            DailyXpDTO dailyXp = dailyXpList.get(i);
            Log.d("StatisticsFragment", "Line chart entry " + i + ": " + dailyXp.getDate() + " = " + dailyXp.getXp() + " XP");
            entries.add(new Entry(i, dailyXp.getXp()));
            
            String dayName = dailyXp.getDate().getDayOfWeek().toString();
            dayName = dayName.substring(0, 1) + dayName.substring(1).toLowerCase();
            labels.add(dayName);
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Daily XP");
        dataSet.setColor(Color.parseColor("#C9A227"));
        dataSet.setCircleColor(Color.parseColor("#7A0F0F"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(6f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#C9A227"));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        LineData data = new LineData(dataSet);
        lineChartDailyXp.setData(data);
        
        XAxis xAxis = lineChartDailyXp.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        
        lineChartDailyXp.getAxisLeft().setAxisMinimum(0f);
        lineChartDailyXp.getAxisRight().setEnabled(false);
        
        Log.d("StatisticsFragment", "Line chart updated with " + entries.size() + " entries");
        lineChartDailyXp.invalidate();
    }
}
