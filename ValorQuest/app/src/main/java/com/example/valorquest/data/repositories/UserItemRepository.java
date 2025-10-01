package com.example.valorquest.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.UserItem;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserItemRepository extends FirebaseRepository<UserItem> {
    public UserItemRepository(String userId) { super("users/" + userId + "/items", UserItem.class); }

    public LiveData<List<UserItem>> getUserItems() {
        MutableLiveData<List<UserItem>> liveData = new MutableLiveData<>();
        getAll(liveData);
        return liveData;
    }

    public void getAllItems(RepositoryCallback<List<UserItem>> callback) {
        db.collection(collectionPath)
                .get()
                .addOnSuccessListener(query -> {
                    List<UserItem> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        list.add(doc.toObject(modelClass));
                    }
                    callback.onComplete(list);
                })
                .addOnFailureListener(e -> callback.onComplete(null));
    }
}
