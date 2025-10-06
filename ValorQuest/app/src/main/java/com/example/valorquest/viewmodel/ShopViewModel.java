package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.BossRepository;
import com.example.valorquest.data.repositories.EquipmentRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.Equipment;
import com.example.valorquest.service.EquipmentService;
import com.example.valorquest.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class ShopViewModel extends ViewModel {
    private static final String WEAPON_TYPE = "weapon";
    private static final String POTION_TYPE = "potion";
    private static final String ARMOR_TYPE = "armor";
    
    private final EquipmentRepository equipmentRepository;
    private final EquipmentService equipmentService;
    private final MutableLiveData<List<Equipment>> allEquipment = new MutableLiveData<>();
    private final MediatorLiveData<List<Equipment>> equipment = new MediatorLiveData<>();
    private final MutableLiveData<Equipment> selectedEquipment = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedEquipmentPrice = new MutableLiveData<>();
    private final MutableLiveData<Boolean> selectedEquipmentPurchasable = new MutableLiveData<>();
    private final MutableLiveData<java.util.Map<String, Integer>> equipmentPrices = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ShopViewModel() {
        equipmentRepository = new EquipmentRepository();
        equipmentService = new EquipmentService(
            equipmentRepository,
            new UserRepository(),
            new BossRepository()
        );

        equipment.addSource(allEquipment, equipments -> {
            if (equipments != null) {
                List<Equipment> filtered = filterNonWeaponEquipment(equipments);
                equipment.setValue(filtered);
                isLoading.setValue(false);
                calculateAllPrices(filtered);
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
        if (eq != null) {
            boolean isPurchasable = isEquipmentPurchasable(eq);
            selectedEquipmentPurchasable.setValue(isPurchasable);
            
            if (isPurchasable) {
                equipmentService.getActualPrice(eq.getId(), selectedEquipmentPrice::postValue);
            } else {
                selectedEquipmentPrice.setValue(0);
            }
        }
    }

    public void clearSelection() {
        selectedEquipment.setValue(null);
        selectedEquipmentPrice.setValue(null);
        selectedEquipmentPurchasable.setValue(null);
    }

    public void purchaseSelectedEquipment(RepositoryCallback<Boolean> callback) {
        Equipment selected = selectedEquipment.getValue();
        if (selected == null) {
            callback.onComplete(false);
            return;
        }
        equipmentService.buyEquipment(selected.getId(), callback);
    }

    private void calculateAllPrices(List<Equipment> equipmentList) {
        java.util.Map<String, Integer> prices = new java.util.HashMap<>();
        final java.util.concurrent.atomic.AtomicInteger completed = new java.util.concurrent.atomic.AtomicInteger(0);
        final java.util.concurrent.atomic.AtomicInteger total = new java.util.concurrent.atomic.AtomicInteger(0);
        
        for (Equipment eq : equipmentList) {
            if (isEquipmentPurchasable(eq)) {
                total.incrementAndGet();
                equipmentService.getActualPrice(eq.getId(), new RepositoryCallback<Integer>() {
                    @Override
                    public void onComplete(Integer price) {
                        // Store the actual price (0 means unavailable for level 0 users)
                        prices.put(eq.getId(), price != null ? price : 0);
                        int currentCompleted = completed.incrementAndGet();
                        if (currentCompleted == total.get()) {
                            equipmentPrices.postValue(prices);
                        }
                    }
                });
            }
        }
        
        if (total.get() == 0) {
            equipmentPrices.postValue(prices);
        }
    }

    private List<Equipment> filterNonWeaponEquipment(List<Equipment> equipments) {
        List<Equipment> filtered = new ArrayList<>();
        for (Equipment eq : equipments) {
            if (!WEAPON_TYPE.equalsIgnoreCase(eq.getType())) {
                filtered.add(eq);
            }
        }
        return filtered;
    }

    public boolean isEquipmentPurchasable(Equipment equipment) {
        return equipment != null && (POTION_TYPE.equals(equipment.getType()) || ARMOR_TYPE.equals(equipment.getType()));
    }

    public LiveData<List<Equipment>> getEquipmentList() { return equipment; }
    public LiveData<Equipment> getSelectedEquipment() { return selectedEquipment; }
    public LiveData<Integer> getSelectedEquipmentPrice() { return selectedEquipmentPrice; }
    public LiveData<Boolean> getSelectedEquipmentPurchasable() { return selectedEquipmentPurchasable; }
    public LiveData<java.util.Map<String, Integer>> getEquipmentPrices() { return equipmentPrices; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
