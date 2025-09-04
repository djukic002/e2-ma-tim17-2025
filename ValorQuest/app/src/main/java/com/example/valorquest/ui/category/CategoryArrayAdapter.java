package com.example.valorquest.ui.category;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.valorquest.R;
import com.example.valorquest.model.Category;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CategoryArrayAdapter extends ArrayAdapter<Category> {

    private final Context context;

    public interface OnCategoryActionListener {
        void onEdit(Category category);
    }

    private final OnCategoryActionListener listener;

    public CategoryArrayAdapter(@NonNull Context context, List<Category> categories, OnCategoryActionListener listener) {
        super(context, 0, categories);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        }

        Category category = getItem(position);

        TextView tvName = itemView.findViewById(R.id.tvCategoryName);
        View viewColor = itemView.findViewById(R.id.viewColor);
        MaterialButton btnEdit = itemView.findViewById(R.id.btnEdit);

        if (category != null) {
            tvName.setText(category.getName());
            try {
                viewColor.getBackground().setTint(Color.parseColor(category.getColor()));
            } catch (IllegalArgumentException e) {
                viewColor.getBackground().setTint(Color.GRAY);
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(category);
                }
            });
        }

        return itemView;
    }
}