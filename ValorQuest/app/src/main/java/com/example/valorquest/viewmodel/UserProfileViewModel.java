package com.example.valorquest.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.model.dto.UserProfileDto;
import com.example.valorquest.model.dto.UserItemDTO;
import com.example.valorquest.service.UserService;
import com.example.valorquest.service.EquipmentService;
import com.example.valorquest.utils.RepositoryCallback;

import java.util.List;

public class UserProfileViewModel extends ViewModel {
    private final UserService userService;
    private final EquipmentService equipmentService;
    private final MutableLiveData<UserProfileDto> profile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    
    // Equipment LiveData
    private final MutableLiveData<List<UserItemDTO>> inventoryEquipment = new MutableLiveData<>();
    private final MutableLiveData<List<UserItemDTO>> equippedEquipment = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEquipmentLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> equipmentMessage = new MutableLiveData<>(null);

    public UserProfileViewModel() {
        this.userService = new UserService();
        this.equipmentService = new EquipmentService(
            new com.example.valorquest.data.repositories.EquipmentRepository(),
            new com.example.valorquest.data.repositories.UserRepository(),
            new com.example.valorquest.data.repositories.BossRepository()
        );
    }

    public void loadProfile(String userId) {
        if (userId == null || userId.isEmpty()) {
            errorMessage.setValue("Invalid userId");
            return;
        }
        isLoading.setValue(true);
        userService.getUserProfile(userId, new RepositoryCallback<UserProfileDto>() {
            @Override
            public void onComplete(UserProfileDto result) {
                if (result == null) {
                    errorMessage.postValue("Failed to load profile");
                } else {
                    profile.postValue(result);
                }
                isLoading.postValue(false);
            }
        });
    }

    public void loadEquipment(String userId) {
        if (userId == null || userId.isEmpty()) {
            equipmentMessage.setValue("Invalid userId");
            return;
        }
        
        isEquipmentLoading.setValue(true);
        
        // Load inactive equipment
        equipmentService.getInactiveEquipment(new RepositoryCallback<List<com.example.valorquest.model.UserItem>>() {
            @Override
            public void onComplete(List<com.example.valorquest.model.UserItem> userItems) {
                Log.d("UserProfileViewModel", "Inactive equipment loaded: " + (userItems != null ? userItems.size() : 0));
                if (userItems != null && !userItems.isEmpty()) {
                    convertToDTOsAsync(userItems, inventoryEquipment::postValue);
                } else {
                    inventoryEquipment.postValue(new java.util.ArrayList<>());
                }
            }
        });
        
        // Load active equipment
        equipmentService.getActiveEquipment(new RepositoryCallback<List<com.example.valorquest.model.UserItem>>() {
            @Override
            public void onComplete(List<com.example.valorquest.model.UserItem> userItems) {
                Log.d("UserProfileViewModel", "Active equipment loaded: " + (userItems != null ? userItems.size() : 0));
                if (userItems != null && !userItems.isEmpty()) {
                    convertToDTOsAsync(userItems, equippedEquipment::postValue);
                } else {
                    equippedEquipment.postValue(new java.util.ArrayList<>());
                }
                isEquipmentLoading.postValue(false);
            }
        });
    }
    
    public void activateEquipment(String userItemId) {
        isEquipmentLoading.setValue(true);
        equipmentService.activateEquipment(userItemId, new RepositoryCallback<com.example.valorquest.model.UserItem>() {
            @Override
            public void onComplete(com.example.valorquest.model.UserItem result) {
                if (result != null) {
                    equipmentMessage.postValue("Equipment activated successfully!");
                    // Reload equipment to refresh both lists
                    String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                    loadEquipment(userId);
                } else {
                    equipmentMessage.postValue("Failed to activate equipment");
                }
                isEquipmentLoading.postValue(false);
            }
        });
    }
    
    public void upgradeWeapon(String userItemId) {
        isEquipmentLoading.setValue(true);
        equipmentService.upgradeWeapon(userItemId, new RepositoryCallback<com.example.valorquest.model.UserItem>() {
            @Override
            public void onComplete(com.example.valorquest.model.UserItem result) {
                if (result != null) {
                    equipmentMessage.postValue("Weapon upgraded successfully!");
                    // Reload equipment to refresh the list
                    String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                    loadEquipment(userId);
                } else {
                    equipmentMessage.postValue("Not enough coins to upgrade weapon");
                }
                isEquipmentLoading.postValue(false);
            }
        });
    }
    
    private void convertToDTOsAsync(List<com.example.valorquest.model.UserItem> userItems, 
                                   java.util.function.Consumer<List<UserItemDTO>> callback) {
        List<UserItemDTO> dtos = new java.util.ArrayList<>();
        final java.util.concurrent.atomic.AtomicInteger completed = new java.util.concurrent.atomic.AtomicInteger(0);
        final int total = userItems.size();
        
        Log.d("UserProfileViewModel", "Converting " + total + " user items to DTOs");
        
        if (total == 0) {
            callback.accept(dtos);
            return;
        }
        
        for (com.example.valorquest.model.UserItem userItem : userItems) {
            Log.d("UserProfileViewModel", "Getting equipment for ID: " + userItem.getEquipmentId());
            new com.example.valorquest.data.repositories.EquipmentRepository().getById(
                userItem.getEquipmentId(), 
                equipment -> {
                    if (equipment != null) {
                        UserItemDTO dto = new UserItemDTO(userItem, equipment);
                        dtos.add(dto);
                        Log.d("UserProfileViewModel", "Created DTO for: " + equipment.getName());
                    } else {
                        Log.d("UserProfileViewModel", "Equipment not found for ID: " + userItem.getEquipmentId());
                    }
                    
                    int currentCompleted = completed.incrementAndGet();
                    Log.d("UserProfileViewModel", "Completed: " + currentCompleted + "/" + total);
                    if (currentCompleted == total) {
                        Log.d("UserProfileViewModel", "All DTOs created, posting " + dtos.size() + " items");
                        callback.accept(dtos);
                    }
                }
            );
        }
    }

    // Getters
    public LiveData<UserProfileDto> getProfile() { return profile; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    
    public LiveData<List<UserItemDTO>> getInventoryEquipment() { return inventoryEquipment; }
    public LiveData<List<UserItemDTO>> getEquippedEquipment() { return equippedEquipment; }
    public LiveData<Boolean> getIsEquipmentLoading() { return isEquipmentLoading; }
    public LiveData<String> getEquipmentMessage() { return equipmentMessage; }
}


