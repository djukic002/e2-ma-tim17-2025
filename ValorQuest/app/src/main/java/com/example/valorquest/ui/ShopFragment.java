package com.example.valorquest.ui;

import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.valorquest.R;
import com.example.valorquest.model.Equipment;
import com.example.valorquest.ui.adapter.EquipmentAdapter;
import com.example.valorquest.viewmodel.ShopViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

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
    }

    private void observeViewModel() {
        viewModel.getEquipmentList().observe(getViewLifecycleOwner(), equipment -> {
            if (equipment != null) {
                adapter.clear();
                adapter.addAll(equipment);
                adapter.notifyDataSetChanged();
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
    }
}