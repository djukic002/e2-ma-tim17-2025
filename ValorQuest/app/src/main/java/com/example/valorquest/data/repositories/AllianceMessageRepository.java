package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.AllianceMessage;

public class AllianceMessageRepository extends FirebaseRepository<AllianceMessage> {
    public AllianceMessageRepository(String allianceId) {
        super("alliances/" + allianceId + "/messages", AllianceMessage.class);
    }
}
