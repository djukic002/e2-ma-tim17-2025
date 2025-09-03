package com.example.valorquest.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.valorquest.R;
import com.example.valorquest.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;

public class AvatarSelectionFragment extends Fragment {

    private AuthViewModel viewModel;
    private View[] avatarContainers = new View[5];
    private View[] avatarChecks = new View[5];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_avatar_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ((AuthActivity) requireActivity()).getViewModel();

        initializeViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initializeViews(View view) {
        // Initialize avatar containers
        avatarContainers[0] = view.findViewById(R.id.avatar_1_container);
        avatarContainers[1] = view.findViewById(R.id.avatar_2_container);
        avatarContainers[2] = view.findViewById(R.id.avatar_3_container);
        avatarContainers[3] = view.findViewById(R.id.avatar_4_container);
        avatarContainers[4] = view.findViewById(R.id.avatar_5_container);

        // Initialize avatar check marks
        avatarChecks[0] = view.findViewById(R.id.avatar_1_check);
        avatarChecks[1] = view.findViewById(R.id.avatar_2_check);
        avatarChecks[2] = view.findViewById(R.id.avatar_3_check);
        avatarChecks[3] = view.findViewById(R.id.avatar_4_check);
        avatarChecks[4] = view.findViewById(R.id.avatar_5_check);

        // Buttons
        MaterialButton backButton = view.findViewById(R.id.btn_back);
        MaterialButton completeButton = view.findViewById(R.id.btn_complete_registration);

        backButton.setOnClickListener(v -> viewModel.goToRegister());
        completeButton.setOnClickListener(v -> viewModel.performRegistration());

        setAvatarInfo();
    }

    private void setAvatarInfo() {
        // Fighter
        TextView fighterName = getView().findViewById(R.id.avatar_1_name);
        TextView fighterDesc = getView().findViewById(R.id.avatar_1_description);
        if (fighterName != null) fighterName.setText("Fighter");
        if (fighterDesc != null) fighterDesc.setText("A skilled warrior with unmatched combat prowess");

        // Rogue
        TextView rogueName = getView().findViewById(R.id.avatar_2_name);
        TextView rogueDesc = getView().findViewById(R.id.avatar_2_description);
        if (rogueName != null) rogueName.setText("Rogue");
        if (rogueDesc != null) rogueDesc.setText("A stealthy assassin with deadly precision");

        // Barbarian
        TextView barbarianName = getView().findViewById(R.id.avatar_3_name);
        TextView barbarianDesc = getView().findViewById(R.id.avatar_3_description);
        if (barbarianName != null) barbarianName.setText("Barbarian");
        if (barbarianDesc != null) barbarianDesc.setText("A fierce berserker with raw strength and fury");

        // Ranger
        TextView rangerName = getView().findViewById(R.id.avatar_4_name);
        TextView rangerDesc = getView().findViewById(R.id.avatar_4_description);
        if (rangerName != null) rangerName.setText("Ranger");
        if (rangerDesc != null) rangerDesc.setText("A wilderness expert with mastery of bow and beast");

        // Mage
        TextView mageName = getView().findViewById(R.id.avatar_5_name);
        TextView mageDesc = getView().findViewById(R.id.avatar_5_description);
        if (mageName != null) mageName.setText("Mage");
        if (mageDesc != null) mageDesc.setText("A powerful spellcaster with ancient arcane knowledge");
    }

    private void setupListeners() {
        // Set up avatar selection listeners
        for (int i = 0; i < 5; i++) {
            final int avatarId = i + 1;
            avatarContainers[i].setOnClickListener(v -> selectAvatar(avatarId));
        }
    }

    private void selectAvatar(int avatarId) {
        viewModel.selectAvatar(avatarId);
        updateAvatarSelectionUI(avatarId);
    }

    private void updateAvatarSelectionUI(int selectedAvatarId) {
        // Hide all checkmarks and deselect all containers
        for (int i = 0; i < 5; i++) {
            avatarChecks[i].setVisibility(View.GONE);
            avatarContainers[i].setSelected(false);
        }

        // Show checkmark and select the chosen avatar
        avatarChecks[selectedAvatarId - 1].setVisibility(View.VISIBLE);
        avatarContainers[selectedAvatarId - 1].setSelected(true);
    }

    private void observeViewModel() {
        viewModel.getSelectedAvatarId().observe(getViewLifecycleOwner(), avatarId -> {
            if (avatarId != null && avatarId != -1) {
                updateAvatarSelectionUI(avatarId);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}