package com.example.valorquest.ui.quests;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.valorquest.R;
import com.example.valorquest.model.dto.DetailedQuestExecutionDto;
import com.example.valorquest.viewmodel.QuestsViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestsCalendarFragment extends Fragment {

    private CalendarView calendarView;
    private QuestsViewModel viewModel;
    private TextView tvSelectedDate;
    private ListView lvQuests;
    private QuestArrayAdapter questAdapter;
    private List<DetailedQuestExecutionDto> allQuests = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quests_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        lvQuests = view.findViewById(R.id.lvQuests);

        questAdapter = new QuestArrayAdapter(requireContext(), new ArrayList<>(),
                quest -> {
                    Toast.makeText(requireContext(),
                            "Clicked: " + quest.questName,
                            Toast.LENGTH_SHORT).show();
                });

        lvQuests.setAdapter(questAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(QuestsViewModel.class);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            viewModel.getDetailedQuestExecutionsForUser(user.getUid())
                    .observe(getViewLifecycleOwner(), quests -> {
                        allQuests = quests;
                        setQuestsOnCalendar(quests);
                    });
        }

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar selected = eventDay.getCalendar();
            LocalDate date = LocalDate.of(
                    selected.get(Calendar.YEAR),
                    selected.get(Calendar.MONTH) + 1,
                    selected.get(Calendar.DAY_OF_MONTH));

            tvSelectedDate.setText(date.toString());

            List<DetailedQuestExecutionDto> filtered = new ArrayList<>();
            for (DetailedQuestExecutionDto q : allQuests) {
                if (q.date.toLocalDate().equals(date)) {
                    filtered.add(q);
                }
            }
            questAdapter.setOriginalList(filtered);
        });
    }




    private void setQuestsOnCalendar(List<DetailedQuestExecutionDto> quests) {
        Map<LocalDate, List<Integer>> dateColorsMap = new HashMap<>();

        for (DetailedQuestExecutionDto dto : quests) {
            dateColorsMap
                    .computeIfAbsent(dto.date.toLocalDate(), k -> new ArrayList<>())
                    .add(Color.parseColor(dto.categoryColor));
        }

        List<EventDay> events = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Integer>> entry : dateColorsMap.entrySet()) {
            Calendar calendar = Calendar.getInstance();
            LocalDate date = entry.getKey();
            calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth(), 0, 0);

            Drawable dot = createPieChartDot(entry.getValue());
            events.add(new EventDay(calendar, dot));
        }

        calendarView.setEvents(events);
    }

    private Drawable createPieChartDot(List<Integer> colors) {
        int size = 48;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int total = colors.size();
        float startAngle = -90f;

        Map<Integer, Integer> colorCounts = new HashMap<>();
        for (int color : colors) {
            colorCounts.put(color, colorCounts.getOrDefault(color, 0) + 1);
        }

        int sum = 0;
        for (int count : colorCounts.values()) {
            sum += count;
        }

        for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
            paint.setColor(entry.getKey());

            float sweepAngle = (entry.getValue() * 360f) / sum;
            canvas.drawArc(0, 0, size, size, startAngle, sweepAngle, true, paint);

            startAngle += sweepAngle;
        }

        return new BitmapDrawable(getResources(), bitmap);
    }
}