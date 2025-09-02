package com.example.valorquest.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.valorquest.R;
import com.example.valorquest.ui.MainActivity;
import com.example.valorquest.viewmodel.AuthViewModel;

public class AuthActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        if (savedInstanceState == null)
            loadFragment(new RegisterFragment());

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getNavigateToAvatarSelection().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                loadFragment(new AvatarSelectionFragment());
                viewModel.resetNavigation();
            }
        });

//        viewModel.getNavigateToComplete().observe(this, shouldNavigate -> {
//            if (shouldNavigate) {
//                loadFragment(new AvatarSelectionFragment());
//                viewModel.resetNavigation();
//            }
//        });

        viewModel.getNavigateToLogin().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                loadFragment(new LoginFragment());
                viewModel.resetNavigation();
            }
        });

        viewModel.getNavigateToRegister().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                loadFragment(new RegisterFragment());
                viewModel.resetNavigation();
            }
        });

//        viewModel.getNavigateToMain().observe(this, shouldNavigate -> {
//            if (shouldNavigate) {
//                // Navigate to MainActivity
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//                finish();
//                viewModel.resetNavigation();
//            }
//        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            // You can show/hide a progress bar here
            if (isLoading != null) {
                // Show loading indicator
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.authFragmentContainer, fragment)
                .commit();
    }

    public AuthViewModel getViewModel() {
        return viewModel;
    }
}