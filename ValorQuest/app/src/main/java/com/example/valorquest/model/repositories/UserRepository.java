package com.example.valorquest.model.repositories;

import com.example.valorquest.model.models.User;

public class UserRepository extends BaseRepository<User> {

    public UserRepository() {
        super("users", User.class); // "users" collection
    }

    // You can add custom methods specific to users here
}
