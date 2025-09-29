package com.example.valorquest.ui;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.valorquest.R;
import com.example.valorquest.model.Result;
import com.example.valorquest.viewmodel.BossFightViewmodel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Main Menu Fragment with 4 navigation buttons
 */
@AndroidEntryPoint
public class MainMenuFragment extends Fragment {

    private BossFightViewmodel bossFightViewmodel;

    public MainMenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);

        MaterialButton btnQuests = view.findViewById(R.id.btnQuests);
        MaterialButton btnShop = view.findViewById(R.id.btnShop);
        MaterialButton btnProfile = view.findViewById(R.id.btnProfile);
        MaterialButton btnCategories = view.findViewById(R.id.btnCategories);
        MaterialButton btnBossFight = view.findViewById(R.id.btnBoss);

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


        setupBossFightButton(btnBossFight);

        return view;
    }

    private void setupBossFightButton(MaterialButton btnBossFight) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        bossFightViewmodel = new ViewModelProvider(this).get(BossFightViewmodel.class);

        if (user == null) {
            btnBossFight.setVisibility(View.GONE);
            return;
        }

        bossFightViewmodel.getActiveBossForUser(user.getUid())
                .observe(getViewLifecycleOwner(), result -> {
                    if (result.getStatus() == Result.Status.SUCCESS && result.getData() != null) {
                        if (btnBossFight.getVisibility() != View.VISIBLE) {
                            btnBossFight.setAlpha(0f);
                            btnBossFight.setVisibility(View.VISIBLE);
                            btnBossFight.animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .start();
                        }
                    } else {
                        btnBossFight.setVisibility(View.GONE);
                    }
                });
    }
}