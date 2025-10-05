package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.model.dto.UserProfileDto;
import com.example.valorquest.service.UserService;
import com.example.valorquest.utils.RepositoryCallback;

public class UserProfileViewModel extends ViewModel {
    private final UserService userService;
    private final MutableLiveData<UserProfileDto> profile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public UserProfileViewModel() {
        this.userService = new UserService();
    }

    public void loadProfile(String userId) {
        if (userId == null || userId.isEmpty()) {
            errorMessage.setValue("Invalid userId");
            return;
        }
        isLoading.setValue(true);
        userService.getUserProfile(userId, new RepositoryCallback<UserProfileDto>() {
            @Override
            public void onComplete(UserProfileDto result) {
                if (result == null) {
                    errorMessage.postValue("Failed to load profile");
                } else {
                    profile.postValue(result);
                }
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<UserProfileDto> getProfile() { return profile; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}


