package com.example.valorquest.ui.quests;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.valorquest.R;
import com.example.valorquest.model.QuestExecution;
import com.example.valorquest.model.QuestWithExecutions;

import java.util.List;

public class QuestArrayAdapter extends ArrayAdapter<QuestWithExecutions> {

    public interface OnQuestClickListener {
        void onQuestClick(QuestWithExecutions quest);
    }

    private final Context context;
    private final OnQuestClickListener listener;

    public QuestArrayAdapter(@NonNull Context context,
                             List<QuestWithExecutions> quests,
                             OnQuestClickListener listener) {
        super(context, 0, quests);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_quest, parent, false);
        }

        QuestWithExecutions questWithExecutions = getItem(position);

        TextView tvName = itemView.findViewById(R.id.tvQuestName);
        TextView tvDate = itemView.findViewById(R.id.tvQuestDate);
        TextView tvStatus = itemView.findViewById(R.id.tvQuestStatus);
        TextView tvImportance = itemView.findViewById(R.id.tvQuestImportance);
        View viewColor = itemView.findViewById(R.id.viewCategoryColor);

        if (questWithExecutions != null) {
            tvName.setText(questWithExecutions.quest.getName());
            tvImportance.setText(questWithExecutions.quest.getImportance().toString());

            if (!questWithExecutions.executions.isEmpty()) {
                QuestExecution execution = questWithExecutions.executions.get(0);
                tvDate.setText(execution.getDate().toString());
                tvStatus.setText(execution.getStatus().toString());
            }

            try {
                int color = Color.parseColor(questWithExecutions.category.getColor());
                GradientDrawable bg = (GradientDrawable) viewColor.getBackground();
                bg.setColor(color);
            } catch (Exception e) {
                viewColor.setBackgroundColor(Color.GRAY);
            }

            itemView.setOnClickListener(v -> listener.onQuestClick(questWithExecutions));
        }

        return itemView;
    }
}