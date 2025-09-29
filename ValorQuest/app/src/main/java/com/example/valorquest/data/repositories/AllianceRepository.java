package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.Alliance;

public class AllianceRepository extends FirebaseRepository<Alliance> {
    public AllianceRepository() {
        super("alliances", Alliance.class);
    }
}
