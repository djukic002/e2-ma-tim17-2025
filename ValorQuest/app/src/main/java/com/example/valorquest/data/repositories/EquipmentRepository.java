package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.Equipment;

public class EquipmentRepository extends FirebaseRepository<Equipment> {
    public EquipmentRepository() { super("equipment", Equipment.class); }
}
