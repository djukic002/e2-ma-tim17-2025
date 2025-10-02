package com.example.valorquest.ui.boss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.UserItemWithEquipmentDto;

import java.util.List;

public class UserItemArrayAdapter extends ArrayAdapter<UserItemWithEquipmentDto> {
    private final Context context;

    public UserItemArrayAdapter(@NonNull Context context, @NonNull List<UserItemWithEquipmentDto> items) {
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

        UserItemWithEquipmentDto dto = getItem(position);
        ImageView ivEquipment = itemView.findViewById(R.id.ivEquipment);
        TextView tvOverlay = itemView.findViewById(R.id.tvOverlay);

        if (dto != null) {
            int drawableId = context.getResources().getIdentifier(dto.getEquipmentId(), "drawable", context.getPackageName());
            ivEquipment.setImageResource(drawableId != 0 ? drawableId : R.drawable.a1);

            tvOverlay.setText("Battles: " + dto.getRemainingBattles() + "\nLvl: " + dto.getUpgradeLevel());

            ivEquipment.setOnClickListener(v ->
                    Toast.makeText(context, dto.getName() + "\n" + dto.getAttribute() + ": +" + dto.getBonus() * 100 + "%", Toast.LENGTH_SHORT).show()
            );
        }

        return itemView;
    }
}
