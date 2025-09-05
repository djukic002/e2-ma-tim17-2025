package com.example.valorquest.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.example.valorquest.R;
import com.example.valorquest.model.Equipment;
import java.util.List;

public class EquipmentAdapter extends ArrayAdapter<Equipment> {
    private final Context context;
    private final List<Equipment> equipmentList;

    public EquipmentAdapter(@NonNull Context context, @NonNull List<Equipment> list) {
        super(context, 0, list);
        this.context = context;
        this.equipmentList = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Equipment equipment = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_equipment_card, parent, false);
        }

        ImageView equipmentImage = convertView.findViewById(R.id.equipment_image);
        TextView equipmentName = convertView.findViewById(R.id.equipment_name);
        TextView equipmentPrice = convertView.findViewById(R.id.equipment_price);
        TextView equipmentType = convertView.findViewById(R.id.equipment_type);

        if (equipment != null) {
            equipmentName.setText(equipment.getName());
            equipmentPrice.setText(String.valueOf(equipment.getCost()));
            equipmentType.setText(equipment.getType());
            equipmentImage.setImageResource(context.getResources().getIdentifier(equipment.getId(), "drawable", context.getOpPackageName()));
        }

        return convertView;
    }
}