package com.example.valorquest.service;

import com.example.valorquest.data.repositories.EquipmentRepository;
import com.example.valorquest.data.repositories.UserItemRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final UserItemRepository userItemRepository;
    private final UserRepository userRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, UserItemRepository userItemRepository, UserRepository userRepository) {
        this.equipmentRepository = equipmentRepository;
        this.userItemRepository = userItemRepository;
        this.userRepository = userRepository;
    }
    public void buyEquipment(String id) {

    }

    public void activateEquipment(String id) {

    }

    public void upgradeEquipment(String id) {

    }

    public void getUserEquipment() {

    }

    public String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
