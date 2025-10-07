package com.example.valorquest.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.UserItemDTO;

import java.util.List;

public class EquippedAdapter extends ArrayAdapter<UserItemDTO> {
    private final Context context;
    private final List<UserItemDTO> items;
    private OnUpgradeListener upgradeListener;

    public interface OnUpgradeListener {
        void onUpgrade(String userItemId);
    }

    public EquippedAdapter(@NonNull Context context, @NonNull List<UserItemDTO> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    public void setUpgradeListener(OnUpgradeListener listener) {
        this.upgradeListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_equipped_equipment, parent, false);
        }

        UserItemDTO item = getItem(position);
        if (item != null) {
            ImageView equipmentImage = convertView.findViewById(R.id.equipment_image);
            TextView equipmentName = convertView.findViewById(R.id.equipment_name);
            TextView equipmentBonus = convertView.findViewById(R.id.equipment_bonus);
            TextView equipmentType = convertView.findViewById(R.id.equipment_type);
            TextView remainingBattles = convertView.findViewById(R.id.remaining_battles);
            Button upgradeButton = convertView.findViewById(R.id.btn_upgrade);

            // Set equipment image
            int resId = context.getResources().getIdentifier(
                    item.getEquipmentId(),
                    "drawable",
                    context.getPackageName()
            );
            if (resId != 0) {
                equipmentImage.setImageResource(resId);
            }

            // Set text content
            equipmentName.setText(item.getEquipmentName());
            equipmentBonus.setText(item.getBonusText());
            equipmentType.setText(item.getEquipmentType());
            remainingBattles.setText(item.getRemainingBattlesText());

            // Show/hide upgrade button for weapons only
            if (item.isUpgradable() && "weapon".equals(item.getEquipmentType())) {
                upgradeButton.setVisibility(View.VISIBLE);
                upgradeButton.setOnClickListener(v -> {
                    if (upgradeListener != null) {
                        upgradeListener.onUpgrade(item.getId());
                    }
                });
            } else {
                upgradeButton.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
