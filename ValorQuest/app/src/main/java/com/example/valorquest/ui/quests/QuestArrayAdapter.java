package com.example.valorquest.ui.quests;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.valorquest.R;
import com.example.valorquest.model.QuestWithExecutions;
import com.example.valorquest.model.dto.DetailedQuestExecutionDto;

import java.util.ArrayList;
import java.util.List;

public class QuestArrayAdapter extends ArrayAdapter<DetailedQuestExecutionDto> {
    private final List<DetailedQuestExecutionDto> originalList = new ArrayList<>();
    private final List<DetailedQuestExecutionDto> displayList = new ArrayList<>();
    private final Context context;
    private final OnQuestClickListener listener;

    public QuestArrayAdapter(@NonNull Context context,
                             List<DetailedQuestExecutionDto> quests,
                             OnQuestClickListener listener) {
        super(context, 0, quests);
        this.context = context;
        this.listener = listener;
        setOriginalList(quests);
    }

    public void setOriginalList(List<DetailedQuestExecutionDto> quests) {
        originalList.clear();
        originalList.addAll(quests);
        displayList.clear();
        displayList.addAll(quests);
        clear();
        addAll(displayList);
        notifyDataSetChanged();
    }

    public void filterByRepeating(String filter) {
        displayList.clear();
        for (DetailedQuestExecutionDto quest : originalList) {
            if (filter.equals("All") ||
                    (filter.equals("Repeating") && quest.isRepeating) ||
                    (filter.equals("Non repeating") && !quest.isRepeating)) {
                displayList.add(quest);
            }
        }
        clear();
        addAll(displayList);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_quest, parent, false);
        }

        DetailedQuestExecutionDto questExecDto = getItem(position);

        TextView tvName = itemView.findViewById(R.id.tvQuestName);
        TextView tvDate = itemView.findViewById(R.id.tvQuestDate);
        TextView tvStatus = itemView.findViewById(R.id.tvQuestStatus);
        TextView tvImportance = itemView.findViewById(R.id.tvQuestImportance);
        View viewColor = itemView.findViewById(R.id.viewCategoryColor);
        viewColor.setBackground(null);

        if (questExecDto != null) {
            tvName.setText(questExecDto.questName);
            tvImportance.setText(questExecDto.importance);

            tvDate.setText(questExecDto.date.toString());
            tvStatus.setText(questExecDto.status);

            try {
                String hex = questExecDto.categoryColor;
                Log.d("color", hex);

                // Ensure we have a GradientDrawable
                Drawable bg = viewColor.getBackground();
                GradientDrawable drawable;

                if (bg instanceof GradientDrawable) {
                    drawable = (GradientDrawable) bg;
                } else {
                    // Create a new oval drawable if current background is not a GradientDrawable
                    drawable = new GradientDrawable();
                    drawable.setShape(GradientDrawable.OVAL);
                    viewColor.setBackground(drawable);
                }

                // Set the color
                int color = Color.parseColor(hex);
                drawable.setColor(color);

            } catch (Exception e) {
                // Fallback color if parsing fails
                GradientDrawable fallbackDrawable = new GradientDrawable();
                fallbackDrawable.setShape(GradientDrawable.OVAL);
                fallbackDrawable.setColor(Color.GRAY);
                viewColor.setBackground(fallbackDrawable);
            }

            itemView.setOnClickListener(v -> listener.onQuestClick(questExecDto));
        }

        return itemView;
    }

    public interface OnQuestClickListener {
        void onQuestClick(DetailedQuestExecutionDto quest);
    }
}