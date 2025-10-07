package com.example.valorquest.ui.shop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.example.valorquest.R;
import com.example.valorquest.model.Equipment;
import com.example.valorquest.viewmodel.ShopViewModel;
import java.util.List;

public class EquipmentAdapter extends ArrayAdapter<Equipment> {
    private final Context context;
    private final List<Equipment> equipmentList;
    private ShopViewModel viewModel;
    private java.util.Map<String, Integer> equipmentPrices = new java.util.HashMap<>();

    public EquipmentAdapter(@NonNull Context context, @NonNull List<Equipment> list) {
        super(context, 0, list);
        this.context = context;
        this.equipmentList = list;
    }

    public void setViewModel(ShopViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void updatePrices(java.util.Map<String, Integer> prices) {
        this.equipmentPrices = prices != null ? prices : new java.util.HashMap<>();
        notifyDataSetChanged();
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
        
        // Find the coins icon (it's in a LinearLayout with the price)
        ViewParent priceContainer = convertView.findViewById(R.id.equipment_price).getParent();
        ImageView coinsIcon = null;
        if (priceContainer instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout priceLayout = (android.widget.LinearLayout) priceContainer;
            if (priceLayout.getChildCount() > 0 && priceLayout.getChildAt(0) instanceof ImageView) {
                coinsIcon = (ImageView) priceLayout.getChildAt(0);
            }
        }

        if (equipment != null) {
            equipmentName.setText(equipment.getName());
            equipmentType.setText(equipment.getType());
            
            boolean isPurchasable = viewModel != null && viewModel.isEquipmentPurchasable(equipment);
            updatePriceDisplay(equipment, isPurchasable, equipmentPrice, coinsIcon);
            
            int resId = context.getResources().getIdentifier(equipment.getId(), "drawable", context.getPackageName());
            if (resId != 0) {
                equipmentImage.setImageResource(resId);
            }
        }

        return convertView;
    }

    private void updatePriceDisplay(Equipment equipment, boolean isPurchasable, TextView priceText, ImageView coinsIcon) {
        if (isPurchasable) {
            Integer price = equipmentPrices.get(equipment.getId());
            if (price != null && price > 0) {
                priceText.setText(String.valueOf(price));
                setCoinsIconVisibility(coinsIcon, View.VISIBLE);
            } else if (price != null && price == 0) {
                priceText.setText("Unavailable");
                setCoinsIconVisibility(coinsIcon, View.GONE);
            } else {
                priceText.setText("Loading...");
                setCoinsIconVisibility(coinsIcon, View.VISIBLE);
            }
        } else {
            priceText.setText("Unavailable");
            setCoinsIconVisibility(coinsIcon, View.GONE);
        }
    }

    private void setCoinsIconVisibility(ImageView coinsIcon, int visibility) {
        if (coinsIcon != null) {
            coinsIcon.setVisibility(visibility);
        }
    }
}