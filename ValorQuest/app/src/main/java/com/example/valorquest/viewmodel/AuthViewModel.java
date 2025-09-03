package com.example.valorquest.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.AuthRepository;
import com.example.valorquest.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;

    private MutableLiveData<User> registrationData = new MutableLiveData<>();
    private MutableLiveData<Integer> selectedAvatarId = new MutableLiveData<>();
    private MutableLiveData<String> registrationPassword = new MutableLiveData<>();

    private MutableLiveData<Boolean> navigateToAvatarSelection = new MutableLiveData<>();
//    private MutableLiveData<Boolean> navigateToComplete = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateToRegister = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>();

    private MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private MutableLiveData<String> loginEmail = new MutableLiveData<>();
    private MutableLiveData<String> loginPassword = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRepository();
        registrationData.setValue(new User());
        selectedAvatarId.setValue(-1);
        isLoading.setValue(false);
    }

    public void updateUsername(String username) {
        User currentUser = registrationData.getValue();
        if (currentUser != null) {
            currentUser.setUsername(username);
            registrationData.setValue(currentUser);
        }
    }

    public void updateEmail(String email) {
        User currentUser = registrationData.getValue();
        if (currentUser != null) {
            currentUser.setEmail(email);
            registrationData.setValue(currentUser);
        }
    }

    public void updateRegistrationPassword(String password) {
        registrationPassword.setValue(password);
    }

    public void updateLoginEmail(String email) {
        loginEmail.setValue(email);
    }

    public void updateLoginPassword(String password) {
        loginPassword.setValue(password);
    }

    public void goBackToLogin() {
        navigateToLogin.setValue(true);
    }

    public void goToRegister() {
        navigateToRegister.setValue(true);
    }

    public void selectAvatar(int avatarId) {
        selectedAvatarId.setValue(avatarId);
        User currentUser = registrationData.getValue();
        if (currentUser != null) {
            currentUser.setAvatarId(avatarId);
            registrationData.setValue(currentUser);
        }
    }

    public void proceedToAvatarSelection() {
        navigateToAvatarSelection.setValue(true);
    }

//    public void completeRegistration() {
//        if (selectedAvatarId.getValue() != null && selectedAvatarId.getValue() != -1) {
//            navigateToComplete.setValue(true);
//        } else {
//            errorMessage.setValue("Please select an avatar");
//        }
//    }

    public void resetNavigation() {
        navigateToAvatarSelection.setValue(false);
//        navigateToComplete.setValue(false);
        navigateToLogin.setValue(false);
        navigateToRegister.setValue(false);
    }

    public void login() {
        String email = getLoginEmail().getValue();
        String password = getLoginPassword().getValue();
        currentUser.setValue(null);

        Log.i("email", email);
        Log.i("password", "Password is: " + password);

        isLoading.setValue(true);

        authRepository.login(email, password, currentUser, errorMessage);

        currentUser.observeForever(firebaseUser -> {
            if (firebaseUser != null) {
                isLoading.setValue(false);
                navigateToMain.setValue(true);
            }
        });

        errorMessage.observeForever(error -> {
            if (error != null && !error.isEmpty()) {
                isLoading.setValue(false);
            }
        });
    }

    public void performRegistration() {
        User user = registrationData.getValue();
        String password = registrationPassword.getValue();

        if (user == null) {
            errorMessage.setValue("Registration data is missing");
            return;
        }

        String email = user.getEmail();
        String username = user.getUsername();
        Integer avatarId = selectedAvatarId.getValue();

        if (email == null || email.isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }

        if (username == null || username.isEmpty()) {
            errorMessage.setValue("Username is required");
            return;
        }

        if (avatarId == null || avatarId == -1) {
            errorMessage.setValue("Please select an avatar");
            return;
        }

        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        isLoading.setValue(true);

        // Use your existing AuthRepository to register
        authRepository.register(email, password, username, avatarId, currentUser, errorMessage);

        // Observe the result
        currentUser.observeForever(firebaseUser -> {
            if (firebaseUser != null) {
                isLoading.setValue(false);
                navigateToLogin.setValue(true);
            }
        });

        errorMessage.observeForever(error -> {
            if (error != null && !error.isEmpty()) {
                isLoading.setValue(false);
            }
        });
    }

    public LiveData<User> getRegistrationData() { return registrationData; }
    public LiveData<Integer> getSelectedAvatarId() { return selectedAvatarId; }

    public LiveData<String> getLoginEmail() {
        return loginEmail;
    }

    public LiveData<String> getLoginPassword() {
        return loginPassword;
    }

    public LiveData<String> getRegistrationPassword() {
        return registrationPassword;
    }
    public LiveData<Boolean> getNavigateToAvatarSelection() { return navigateToAvatarSelection; }
//    public LiveData<Boolean> getNavigateToComplete() { return navigateToComplete; }
    public LiveData<Boolean> getNavigateToLogin() { return navigateToLogin; }
    public LiveData<Boolean> getNavigateToRegister() { return navigateToRegister; }
    public LiveData<Boolean> getNavigateToMain() { return navigateToMain; }
    public LiveData<FirebaseUser> getCurrentUser() { return currentUser; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
}
