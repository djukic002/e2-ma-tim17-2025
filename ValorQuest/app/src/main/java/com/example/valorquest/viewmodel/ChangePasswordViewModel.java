package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> passwordChanged = new MutableLiveData<>(false);

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        // Reset previous states
        errorMessage.setValue(null);
        successMessage.setValue(null);
        passwordChanged.setValue(false);

        // Validation
        if (newPassword == null || newPassword.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters long");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            errorMessage.setValue("New passwords do not match");
            return;
        }

        if (currentPassword != null && currentPassword.equals(newPassword)) {
            errorMessage.setValue("New password must be different from current password");
            return;
        }

        isLoading.setValue(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            errorMessage.setValue("No user logged in");
            isLoading.setValue(false);
            return;
        }

        // Update password
        user.updatePassword(newPassword)
            .addOnCompleteListener(task -> {
                isLoading.setValue(false);
                if (task.isSuccessful()) {
                    successMessage.setValue("Password changed successfully");
                    passwordChanged.setValue(true);
                } else {
                    String error = task.getException() != null ? 
                        task.getException().getMessage() : "Failed to change password";
                    errorMessage.setValue(error);
                }
            });
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    // Getters
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getPasswordChanged() { return passwordChanged; }
}
