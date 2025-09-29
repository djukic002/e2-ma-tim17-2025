package com.example.valorquest.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.valorquest.R;
import com.google.android.material.button.MaterialButton;

/**
 * Main Menu Fragment with 4 navigation buttons
 */
public class MainMenuFragment extends Fragment {

    public MainMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);

        // Initialize all 4 buttons
        MaterialButton btnQuests = view.findViewById(R.id.btnQuests);
        MaterialButton btnShop = view.findViewById(R.id.btnShop);
        MaterialButton btnProfile = view.findViewById(R.id.btnProfile);
        MaterialButton btnCategories = view.findViewById(R.id.btnCategories);
        MaterialButton btnBossFight = view.findViewById(R.id.btnBoss);
        MaterialButton btnSocial = view.findViewById(R.id.btnSocial);

        // Set click listeners for navigation
        btnQuests.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_mainMenuFragment_to_questsFragment));

        btnProfile.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_mainMenuFragment_to_profileFragment));

        btnShop.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_mainMenuFragment_to_shopFragment));

        btnCategories.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_mainMenuFragment_to_categoryFragment));

        btnBossFight.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_mainMenuFragment_to_bossFragment));

        btnSocial.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_mainMenuFragment_to_socialFragment));

        return view;
    }
}