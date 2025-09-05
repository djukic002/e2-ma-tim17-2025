package com.example.valorquest.data;

import com.example.valorquest.model.Equipment;
import com.example.valorquest.data.repositories.EquipmentRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class EquipmentUploader {

    public static void uploadAllEquipment() {
        EquipmentRepository repository = new EquipmentRepository();
        List<Equipment> allEquipment = EquipmentData.getAllEquipment();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Add all equipment to batch
        for (Equipment eq : allEquipment) {
            // Reference to document with equipment ID
            batch.set(db.collection("equipment").document(eq.getId()), eq);
        }

        // Commit the batch in one call
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("All equipment saved successfully!");
            } else {
                System.err.println("Failed to save equipment: " + task.getException());
            }
        });
    }
}

