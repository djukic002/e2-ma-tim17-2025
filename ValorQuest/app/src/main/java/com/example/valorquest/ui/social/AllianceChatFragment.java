package com.example.valorquest.ui.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.AllianceMessageDto;
import com.example.valorquest.service.AllianceService;
import com.example.valorquest.viewmodel.AllianceChatViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

public class AllianceChatFragment extends Fragment {
    private AllianceChatViewModel viewModel;
    private AllianceChatAdapter adapter;
    private ListView messagesList;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private ProgressBar loadingIndicator;
    
    private String allianceId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get alliance ID from arguments
        if (getArguments() != null) {
            allianceId = getArguments().getString("allianceId");
        }
        
        if (allianceId == null) {
            Toast.makeText(getContext(), "No alliance ID provided", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }
        
        // Set keyboard handling
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        initViews(view);
        setupViewModel();
        setupListeners();
        observeViewModel();
        
        // Set alliance ID and load messages
        viewModel.setAllianceId(allianceId);
    }
    
    private void initViews(View view) {
        messagesList = view.findViewById(R.id.messages_list);
        messageInput = view.findViewById(R.id.et_message);
        sendButton = view.findViewById(R.id.btn_send);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        
        // Initialize adapter
        adapter = new AllianceChatAdapter(getContext(), null);
        messagesList.setAdapter(adapter);
        
        // Auto-scroll to bottom when entering chat
        messagesList.post(() -> {
            if (adapter.getCount() > 0) {
                messagesList.setSelection(adapter.getCount() - 1);
            }
        });
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AllianceChatViewModel.class);
    }
    
    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        
        // Auto-scroll to bottom when new messages arrive
        messagesList.setOnItemClickListener((parent, view, position, id) -> {
            messagesList.smoothScrollToPosition(adapter.getCount() - 1);
        });
    }
    
    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                adapter.updateMessages(messages);
                // Auto-scroll to bottom
                messagesList.post(() -> {
                    if (adapter.getCount() > 0) {
                        messagesList.smoothScrollToPosition(adapter.getCount() - 1);
                    }
                });
            }
        });
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingIndicator.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
            } else {
                loadingIndicator.setVisibility(View.GONE);
                sendButton.setEnabled(true);
            }
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            viewModel.sendMessage(messageText);
            messageInput.setText(""); // Clear input
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Restore default keyboard mode when leaving chat
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }
}
