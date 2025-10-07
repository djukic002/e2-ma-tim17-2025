package com.example.valorquest.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.valorquest.R;
import com.example.valorquest.viewmodel.ChangePasswordViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordFragment extends Fragment {
    private ChangePasswordViewModel viewModel;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnChangePassword;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(ChangePasswordViewModel.class);
        
        initializeViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initializeViews(View view) {
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
    }

    private void setupListeners() {
        btnChangePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText() != null ? 
                etNewPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? 
                etConfirmPassword.getText().toString().trim() : "";
            
            viewModel.changePassword(null, newPassword, confirmPassword);
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnChangePassword.setEnabled(!isLoading);
            btnChangePassword.setText(isLoading ? "Changing..." : "Change Password");
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getPasswordChanged().observe(getViewLifecycleOwner(), passwordChanged -> {
            if (passwordChanged) {
                // Show success message and logout
                Toast.makeText(requireContext(), 
                    "Password changed successfully! You will be logged out now.", 
                    Toast.LENGTH_LONG).show();
                
                // Logout user
                viewModel.logout();
                
                // Navigate to splash activity (user will need to log in again)
                android.content.Intent intent = new android.content.Intent(requireContext(), com.example.valorquest.ui.SplashActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }
}
