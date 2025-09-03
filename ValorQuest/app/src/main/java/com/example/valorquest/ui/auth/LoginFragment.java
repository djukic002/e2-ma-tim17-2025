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

public class LoginFragment extends Fragment {

    private AuthViewModel viewModel;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton, goToRegisterButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ((AuthActivity) requireActivity()).getViewModel();
        initializeViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initializeViews(View view) {
        emailInput = view.findViewById(R.id.et_email);
        passwordInput = view.findViewById(R.id.et_password);
        loginButton = view.findViewById(R.id.btn_login);
        goToRegisterButton = view.findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                viewModel.updateLoginEmail(emailInput.getText().toString());
        });

        passwordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.updateLoginPassword(passwordInput.getText().toString());
            }
        });

        goToRegisterButton.setOnClickListener(v -> {
            viewModel.goToRegister();
        });

        loginButton.setOnClickListener(v -> {
            viewModel.updateLoginEmail(emailInput.getText().toString());
            viewModel.updateLoginPassword(passwordInput.getText().toString());
            viewModel.login();
        });
    }

    private void observeViewModel() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loginButton.setEnabled(!isLoading);
                if (isLoading)
                    loginButton.setText("Logging in...");
                else
                    loginButton.setText("Enter");
            }
        });

        viewModel.getLoginPassword().observe(getViewLifecycleOwner(), password -> {
            if (password != null && !password.isEmpty()) {
                passwordInput.setText(password);
            }
        });

        viewModel.getLoginEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null && !email.isEmpty()) {
                emailInput.setText(email);
            }
        });
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }
}