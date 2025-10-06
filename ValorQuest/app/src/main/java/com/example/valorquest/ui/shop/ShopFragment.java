package com.example.valorquest.ui.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.valorquest.R;
import com.example.valorquest.model.Equipment;
import com.example.valorquest.utils.RepositoryCallback;
import com.example.valorquest.viewmodel.ShopViewModel;
import com.google.android.material.button.MaterialButton;
import android.widget.Toast;

import java.util.ArrayList;

public class ShopFragment extends Fragment {
    private ShopViewModel viewModel;
    private EquipmentAdapter adapter;
    private ListView listView;
    private TextView noEquipmentSelected;
    private LinearLayout equipmentDetailsContainer;
    private ImageView equipmentImage;
    private TextView equipmentName, equipmentBonus, equipmentType, equipmentDescription;
    private MaterialButton purchaseButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        adapter = new EquipmentAdapter(requireContext(), new ArrayList<>());
        adapter.setViewModel(viewModel);

        initializeViews(root);

        listView.setAdapter(adapter);

        setupListeners();
        observeViewModel();
    }

    private void initializeViews(View view) {
        listView = view.findViewById(R.id.lv_equipment);
        noEquipmentSelected = view.findViewById(R.id.no_equipment_selected);
        equipmentDetailsContainer = view.findViewById(R.id.equipment_details_container);

        equipmentImage = view.findViewById(R.id.equipment_details_image);
        equipmentName = view.findViewById(R.id.equipment_details_name);
        equipmentBonus = view.findViewById(R.id.equipment_details_bonus);
        equipmentType = view.findViewById(R.id.equipment_details_type);
        equipmentDescription = view.findViewById(R.id.equipment_details_description);
        purchaseButton = view.findViewById(R.id.btn_purchase);
    }

    private void setupListeners() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Equipment selected = adapter.getItem(position);

            Equipment current = viewModel.getSelectedEquipment().getValue();
            if (current != null && current.equals(selected))
                viewModel.clearSelection();
            else
                viewModel.selectEquipment(selected);
        });

        purchaseButton.setOnClickListener(v -> {
            viewModel.purchaseSelectedEquipment(new RepositoryCallback<Boolean>() {
                @Override
                public void onComplete(Boolean success) {
                    if (success) {
                        Toast.makeText(requireContext(), "Equipment purchased successfully!", Toast.LENGTH_SHORT).show();
                        viewModel.clearSelection();
                    } else {
                        Toast.makeText(requireContext(), "Failed to purchase equipment. Check your coins!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void observeViewModel() {
        viewModel.getEquipmentList().observe(getViewLifecycleOwner(), equipment -> {
            if (equipment != null) {
                adapter.clear();
                adapter.addAll(equipment);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getEquipmentPrices().observe(getViewLifecycleOwner(), prices -> {
            if (prices != null) {
                adapter.updatePrices(prices);
            }
        });

        viewModel.getSelectedEquipment().observe(getViewLifecycleOwner(), eq -> {
            if (eq == null) {
                // No selection
                equipmentDetailsContainer.setVisibility(View.GONE);
                noEquipmentSelected.setVisibility(View.VISIBLE);
            } else {
                // Show details
                equipmentDetailsContainer.setVisibility(View.VISIBLE);
                noEquipmentSelected.setVisibility(View.GONE);

                equipmentName.setText(eq.getName());
                equipmentBonus.setText(eq.getAttribute() + " +" + String.valueOf(eq.getBonus() * 100) + "%");
                equipmentType.setText(eq.getType());
                equipmentDescription.setText(eq.getDescription());
                purchaseButton.setText("Purchase " + eq.getCost());

                int resId = getResources().getIdentifier(
                        eq.getId(),
                        "drawable",
                        requireContext().getPackageName()
                );

                if (resId != 0)
                    equipmentImage.setImageResource(resId);
            }
        });

        viewModel.getSelectedEquipmentPurchasable().observe(getViewLifecycleOwner(), isPurchasable -> {
            if (isPurchasable != null) {
                updatePurchaseButtonState(isPurchasable, viewModel.getSelectedEquipmentPrice().getValue());
            }
        });

        viewModel.getSelectedEquipmentPrice().observe(getViewLifecycleOwner(), price -> {
            Boolean isPurchasable = viewModel.getSelectedEquipmentPurchasable().getValue();
            if (isPurchasable != null && isPurchasable) {
                updatePurchaseButtonState(true, price);
            }
        });
    }

    private void updatePurchaseButtonState(boolean isPurchasable, Integer price) {
        if (isPurchasable) {
            if (price != null && price > 0) {
                purchaseButton.setEnabled(true);
                purchaseButton.setText("Purchase " + price);
            } else if (price != null && price == 0) {
                purchaseButton.setEnabled(false);
                purchaseButton.setText("Unavailable");
                purchaseButton.setIcon(null);
            } else {
                purchaseButton.setEnabled(true);
                purchaseButton.setText("Loading...");
            }
        } else {
            purchaseButton.setEnabled(false);
            purchaseButton.setText("Unavailable");
        }
    }
}