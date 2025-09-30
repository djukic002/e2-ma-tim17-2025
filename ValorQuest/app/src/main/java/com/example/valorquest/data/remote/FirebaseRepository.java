package com.example.valorquest.data.remote;

import androidx.lifecycle.MutableLiveData;

import com.example.valorquest.model.User;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRepository<T> {
    private final FirebaseFirestore db;
    private final String collectionPath;
    private final Class<T> modelClass;

    public FirebaseRepository(String collectionPath, Class<T> modelClass) {
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
    public void getById(String id, RepositoryCallback<T> callback) {
        db.collection(collectionPath).document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onComplete(doc.toObject(modelClass));
                    } else {
                        callback.onComplete(null);
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(null));
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

    public void getAll(RepositoryCallback<List<T>> callback) {
        db.collection(collectionPath)
                .get()
                .addOnSuccessListener(query -> {
                    List<T> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        T obj = doc.toObject(modelClass);
                        if (obj != null) list.add(obj);
                    }
                    callback.onComplete(list);
                })
                .addOnFailureListener(e -> callback.onComplete(null));
    }

    // DELETE
    public void delete(String id, OnCompleteListener<Void> listener) {
        db.collection(collectionPath).document(id)
                .delete()
                .addOnCompleteListener(listener);
    }

    public void removeTokenFromOtherUsers(String currentUserId, String token, OnCompleteListener<Void> listener) {
        db.collection("users")
                .whereArrayContains("fcmTokens", token)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : task.getResult()) {
                            if (!doc.getId().equals(currentUserId)) {
                                User otherUser = doc.toObject(User.class);
                                if (otherUser != null && otherUser.getFcmTokens().contains(token)) {
                                    otherUser.getFcmTokens().remove(token);
                                    batch.set(doc.getReference(), otherUser);
                                }
                            }
                        }
                        batch.commit().addOnCompleteListener(listener);
                    } else {
                        // Still call listener so login flow continues
                        listener.onComplete(null);
                    }
                });
    }
}
