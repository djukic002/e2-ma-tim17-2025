package com.example.valorquest.data.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.valorquest.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.time.LocalDateTime;

public class AuthRepository {
    private FirebaseAuth auth;
    private UserRepository userRepository;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
    }

    public void register(String email, String password, String username,
                         int avatarId,
                         MutableLiveData<FirebaseUser> userLiveData,
                         MutableLiveData<String> errorLiveData) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification();
                            // Create User object
                            User user = new User(firebaseUser.getUid(), email, username, avatarId);
                            user.setPreviousLeveledUpAt(Timestamp.now());
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

    public void login(String email, String password,
                      MutableLiveData<FirebaseUser> userLiveData,
                      MutableLiveData<String> errorLiveData) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            if (firebaseUser.isEmailVerified()) {
                                // Login successful
                                userLiveData.postValue(firebaseUser);

                                // --- FCM token handling ---
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(tokenTask -> {
                                            if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                                String token = tokenTask.getResult();

                                                // Step 1: Remove this token from *all other users*
                                                userRepository.removeTokenFromOtherUsers(firebaseUser.getUid(), token,
                                                        removeTask -> {
                                                            // Step 2: Add token to this user (if not already present)
                                                            userRepository.getById(firebaseUser.getUid(), user -> {
                                                                if (user != null) {
                                                                    if (!user.getFcmTokens().contains(token)) {
                                                                        user.getFcmTokens().add(token);
                                                                        userRepository.save(user.getId(), user, saveTask -> {
                                                                            // Optional: log success/failure
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        });
                                            }
                                        });

                            } else {
                                errorLiveData.postValue("Please verify your email before logging in.");
                            }
                        } else {
                            errorLiveData.postValue("Login failed. User not found.");
                        }
                    } else {
                        errorLiveData.postValue(task.getException().getMessage());
                    }
                });
    }


}


