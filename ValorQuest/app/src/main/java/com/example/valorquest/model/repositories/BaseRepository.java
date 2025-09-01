package com.example.valorquest.model.repositories;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BaseRepository<T> {
    private final FirebaseFirestore db;
    private final String collectionPath;
    private final Class<T> modelClass;

    public BaseRepository(String collectionPath, Class<T> modelClass) {
        this.db = FirebaseFirestore.getInstance();
        this.collectionPath = collectionPath;
        this.modelClass = modelClass;
    }

    // CREATE / UPDATE
    public void save(String id, T object, OnCompleteListener<Void> listener) {
        db.collection(collectionPath).document(id)
                .set(object)
                .addOnCompleteListener(listener);
    }

    // READ single
    public void getById(String id, MutableLiveData<T> liveData) {
        db.collection(collectionPath).document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        liveData.postValue(doc.toObject(modelClass));
                    } else {
                        liveData.postValue(null);
                    }
                })
                .addOnFailureListener(e -> liveData.postValue(null));
    }

    // READ all
    public void getAll(MutableLiveData<List<T>> liveData) {
        db.collection(collectionPath)
                .get()
                .addOnSuccessListener(query -> {
                    List<T> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        list.add(doc.toObject(modelClass));
                    }
                    liveData.postValue(list);
                })
                .addOnFailureListener(e -> liveData.postValue(null));
    }

    // DELETE
    public void delete(String id, OnCompleteListener<Void> listener) {
        db.collection(collectionPath).document(id)
                .delete()
                .addOnCompleteListener(listener);
    }
}
