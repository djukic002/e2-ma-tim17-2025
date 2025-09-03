package com.example.valorquest.ui.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {

    private AuthViewModel viewModel;
    private TextInputEditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private MaterialButton registerButton, backToLoginButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ((AuthActivity) requireActivity()).getViewModel();
        initializeViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initializeViews(View view) {
        usernameInput = view.findViewById(R.id.et_username);
        emailInput = view.findViewById(R.id.et_email);
        passwordInput = view.findViewById(R.id.et_password);
        confirmPasswordInput = view.findViewById(R.id.et_confirm_password);
        registerButton = view.findViewById(R.id.btn_register);
        backToLoginButton = view.findViewById(R.id.btn_go_to_login);
    }

    private void setupListeners() {
        // Input listeners
        usernameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.updateUsername(usernameInput.getText().toString());
            }
        });

        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.updateEmail(emailInput.getText().toString());
            }
        });

        // Register button
        registerButton.setOnClickListener(v -> {
            if (validateInputs())
                viewModel.proceedToAvatarSelection();
        });

        // Back to login
        backToLoginButton.setOnClickListener(v -> {
            viewModel.goBackToLogin();
        });

        passwordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.updateRegistrationPassword(passwordInput.getText().toString());
            }
        });
    }

    private boolean validateInputs() {
        String username = usernameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords don't match");
            return false;
        }

        return true;
    }

    private void observeViewModel() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                registerButton.setEnabled(!isLoading);
                if (isLoading) {
                    registerButton.setText("Creating Account...");
                } else {
                    registerButton.setText("Enter");
                }
            }
        });

        viewModel.getRegistrationData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Populate form fields with existing data
                if (usernameInput != null && user.getUsername() != null) {
                    usernameInput.setText(user.getUsername());
                }
                if (emailInput != null && user.getEmail() != null) {
                    emailInput.setText(user.getEmail());
                }
            }
        });

        viewModel.getRegistrationPassword().observe(getViewLifecycleOwner(), password -> {
            if (password != null && !password.isEmpty()) {
                passwordInput.setText(password);
            }
        });
    }

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }
}