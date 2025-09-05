package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.UserItem;

public class UserItemRepository extends FirebaseRepository<UserItem> {
    public UserItemRepository(String userId) { super("users/" + userId + "/items", UserItem.class); }
}
