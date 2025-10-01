package com.example.valorquest.ui.boss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.valorquest.R;
import com.example.valorquest.model.UserItem;

import java.util.List;

public class UserItemArrayAdapter extends ArrayAdapter<UserItem> {
    private final Context context;

    public UserItemArrayAdapter(@NonNull Context context, @NonNull List<UserItem> items) {
        super(context, 0, items);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_user_equipment, parent, false);
        }

        UserItem item = getItem(position);
        ImageView ivEquipment = itemView.findViewById(R.id.ivEquipment);

        if (item != null) {
            // Lookup drawable by equipmentId
            int drawableId = context.getResources().getIdentifier(
                    item.getEquipmentId(), "drawable", context.getPackageName()
            );

            if (drawableId != 0) {
                ivEquipment.setImageResource(drawableId);
            } else {
                ivEquipment.setImageResource(R.drawable.a1);
            }

            // Optional: click listener for the item
            ivEquipment.setOnClickListener(v -> {
                System.out.println("Clicked on item: " + item.getEquipmentId());
            });
        }

        return itemView;
    }
}