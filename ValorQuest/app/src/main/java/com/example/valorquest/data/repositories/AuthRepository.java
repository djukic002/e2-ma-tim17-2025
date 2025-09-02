package com.example.valorquest.data.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.valorquest.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {
    private FirebaseAuth auth;
    private UserRepository userRepository;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
    }

    public void register(String email, String password, String name,
                         MutableLiveData<FirebaseUser> userLiveData,
                         MutableLiveData<String> errorLiveData) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Create User object
                            User user = new User(firebaseUser.getUid(), name, email);

                            // Use BaseRepository save method
                            userRepository.save(firebaseUser.getUid(), user, (OnCompleteListener<Void>) firestoreTask -> {
                                if (firestoreTask.isSuccessful()) {
                                    userLiveData.postValue(firebaseUser);
                                } else {
                                    errorLiveData.postValue(firestoreTask.getException().getMessage());
                                }
                            });
                        }
                    } else {
                        errorLiveData.postValue(task.getException().getMessage());
                    }
                });
    }
}


