package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.User;

public class UserRepository extends FirebaseRepository<User> {

    public UserRepository() {
        super("users", User.class); // "users" collection
    }

    // You can add custom methods specific to users here
}
