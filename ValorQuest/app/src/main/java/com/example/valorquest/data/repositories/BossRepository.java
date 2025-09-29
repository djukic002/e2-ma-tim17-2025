package com.example.valorquest.data.repositories;

import com.example.valorquest.data.remote.FirebaseRepository;
import com.example.valorquest.model.Boss;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentSnapshot;

public class BossRepository extends FirebaseRepository<Boss> {

    public BossRepository() {
        super("bosses", Boss.class);
    }

    // custom methods
    public void getMaxLevelBossForUser(String userId, RepositoryCallback<Boss> callback) {
        db.collection(collectionPath)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    Boss maxBoss = null;

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Boss boss = doc.toObject(modelClass);
                        if (boss != null) {
                            boss.setId(doc.getId());

                            if (maxBoss == null || boss.getLevel() > maxBoss.getLevel()) {
                                maxBoss = boss;
                            }
                        }
                    }

                    callback.onComplete(maxBoss);
                })
                .addOnFailureListener(e -> callback.onComplete(null));
    }

}
