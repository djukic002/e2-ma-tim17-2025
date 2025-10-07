package com.example.valorquest.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.AllianceNotificationRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.model.dto.AllianceMessageDto;
import com.example.valorquest.service.AllianceMissionService;
import com.example.valorquest.service.AllianceService;
import com.example.valorquest.service.FriendService;
import com.example.valorquest.utils.RepositoryCallback;

import java.util.List;

import javax.inject.Inject;

public class AllianceChatViewModel extends ViewModel {
    private final AllianceService allianceService;
    private final MutableLiveData<List<AllianceMessageDto>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private String allianceId;

    public AllianceChatViewModel() {
        this.allianceService = new AllianceService(new AllianceRepository(), new AllianceNotificationRepository(), new UserRepository(), new FriendService(new UserRepository()), new AllianceMissionService());
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
        loadMessages();
    }

    public void loadMessages() {
        if (allianceId == null) {
            error.setValue("No alliance ID provided");
            return;
        }

        isLoading.setValue(true);
        allianceService.getAllianceMessages(allianceId, new RepositoryCallback<List<AllianceMessageDto>>() {
            @Override
            public void onComplete(List<AllianceMessageDto> result) {
                isLoading.setValue(false);
                if (result != null) {
                    messages.setValue(result);
                } else {
                    error.setValue("Failed to load messages");
                }
            }
        });
    }

    public void sendMessage(String messageText) {
        if (allianceId == null || messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        allianceService.sendMessage(allianceId, messageText.trim(), new RepositoryCallback<Boolean>() {
            @Override
            public void onComplete(Boolean success) {
                if (success) {
                    // Reload messages after sending
                    loadMessages();
                } else {
                    error.setValue("Failed to send message");
                }
            }
        });
    }

    public LiveData<List<AllianceMessageDto>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }
}
