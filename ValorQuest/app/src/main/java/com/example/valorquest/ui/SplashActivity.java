package com.example.valorquest.ui;

import android.content.Intent;
import android.media.audiofx.DynamicsProcessing;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.valorquest.data.EquipmentUploader;
import com.example.valorquest.ui.auth.AuthActivity;
import com.example.valorquest.ui.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

//        EquipmentUploader.uploadAllEquipment();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is logged in AND email is verified
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User not logged in or email not verified
            startActivity(new Intent(this, AuthActivity.class));
        }

        finish(); // Prevent going back to splash
    }
}
