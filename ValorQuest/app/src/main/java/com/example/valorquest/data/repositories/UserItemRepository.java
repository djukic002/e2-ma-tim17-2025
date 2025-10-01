package com.example.valorquest.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.UserItem;

import java.util.List;

public class UserItemRepository extends FirebaseRepository<UserItem> {
    public UserItemRepository(String userId) { super("users/" + userId + "/items", UserItem.class); }

    public LiveData<List<UserItem>> getUserItems() {
        MutableLiveData<List<UserItem>> liveData = new MutableLiveData<>();
        getAll(liveData);
        return liveData;
    }
}
