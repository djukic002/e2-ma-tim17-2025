package com.example.valorquest.data.repositories;

import android.util.Log;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.MissionContribution;
import com.example.valorquest.model.enums.MissionContributionType;
import com.example.valorquest.utils.RepositoryCallback;

public class MissionContributionRepository extends FirebaseRepository<MissionContribution> {
    public MissionContributionRepository(String allianceId, String missionId) {
        super("alliances/" + allianceId + "/missions/" + missionId + "/contributions", MissionContribution.class);
    }
    public void countUserContributions(String userId, MissionContributionType actionType, int limit,
                                       RepositoryCallback<Boolean> callback) {
        db.collection(collectionPath)
                .whereEqualTo("userId", userId)
                .whereEqualTo("actionType", actionType.name())
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    boolean canContribute = count < limit;
                    callback.onComplete(canContribute);
                })
                .addOnFailureListener(e -> {
                    Log.e("MissionContributionRepo", "Failed to count contributions", e);
                    callback.onComplete(false);
                });
    }
}