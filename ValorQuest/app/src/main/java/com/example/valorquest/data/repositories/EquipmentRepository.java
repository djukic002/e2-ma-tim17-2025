package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.Equipment;
import com.example.valorquest.utils.RepositoryCallback;

public class EquipmentRepository extends FirebaseRepository<Equipment> {
    public EquipmentRepository() { super("equipment", Equipment.class); }

    public void getById(String id, RepositoryCallback<Equipment> callback) {
        super.getById(id, callback);
    }
}
