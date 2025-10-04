package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.AllianceMission;

public class AllianceMissionRepository extends FirebaseRepository<AllianceMission> {
    public AllianceMissionRepository(String allianceId) {
        super("alliances/" + allianceId + "/missions", AllianceMission.class);
    }
}