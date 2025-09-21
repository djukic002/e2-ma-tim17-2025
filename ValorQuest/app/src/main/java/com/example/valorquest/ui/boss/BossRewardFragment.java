package com.example.valorquest.ui.boss;

import android.media.SoundPool;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.google.android.material.button.MaterialButton;


public class BossRewardFragment extends Fragment {
    private ImageView ivChest;
    private MaterialButton btnOpenChest;
    private SoundPool soundPool;
    private int chestOpenSound;
    private boolean isOpened = false;
    private MaterialButton btnClaimChest;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boss_reward, container, false);

        ivChest = view.findViewById(R.id.ivChest);
        btnOpenChest = view.findViewById(R.id.btnOpenChest);
        btnClaimChest = view.findViewById(R.id.btnClaimChest);

        // Setup SoundPool
        soundPool = new SoundPool.Builder().setMaxStreams(3).build();
        chestOpenSound = soundPool.load(requireContext(), R.raw.chest_open, 1);

        // Button click
        btnOpenChest.setOnClickListener(v -> openChest());

        // Claim button click
        btnClaimChest.setOnClickListener(v -> claimChest());

        return view;
    }

    private void openChest() {
        if (isOpened) return;

        isOpened = true;

        ivChest.setImageResource(R.drawable.chest_open);
        soundPool.play(chestOpenSound, 1f, 1f, 1, 0, 1f);

        btnOpenChest.setVisibility(View.GONE);
        btnClaimChest.setVisibility(View.VISIBLE);
    }

    private void claimChest() {
        Toast.makeText(requireContext(), "You claimed the chest!", Toast.LENGTH_SHORT).show();
        btnClaimChest.setEnabled(false);

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_bossRewardFragment_to_mainMenuFragment);
    }
}