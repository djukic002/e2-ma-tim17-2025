package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.EquipmentRepository;
import com.example.valorquest.model.Equipment;

import java.util.ArrayList;
import java.util.List;

public class ShopViewModel extends ViewModel {
    private final EquipmentRepository equipmentRepository;
    private final MutableLiveData<List<Equipment>> allEquipment = new MutableLiveData<>();
    private final MediatorLiveData<List<Equipment>> equipment = new MediatorLiveData<>();
    private final MutableLiveData<Equipment> selectedEquipment = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ShopViewModel() {
        equipmentRepository = new EquipmentRepository();

        equipment.addSource(allEquipment, equipments -> {
            if (equipments != null) {
                List<Equipment> filtered = new ArrayList<>();
                for (Equipment eq : equipments) {
                    if (!"weapon".equalsIgnoreCase(eq.getType())) {
                        filtered.add(eq);
                    }
                }
                equipment.setValue(filtered);
                isLoading.setValue(false);
            } else {
                errorMessage.setValue("Failed to load equipment");
                isLoading.setValue(false);
            }
        });

        loadEquipment();
    }

    public void loadEquipment() {
        isLoading.setValue(true);
        equipmentRepository.getAll(allEquipment);
    }

    public void selectEquipment(Equipment eq) {
        selectedEquipment.setValue(eq);
    }

    public void clearSelection() {
        selectedEquipment.setValue(null);
    }

    public LiveData<List<Equipment>> getEquipmentList() { return equipment; }
    public LiveData<Equipment> getSelectedEquipment() { return selectedEquipment; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
