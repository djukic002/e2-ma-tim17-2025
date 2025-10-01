package com.example.valorquest.ui.social;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.model.User;
import com.example.valorquest.viewmodel.AllianceCreationViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllianceCreationFragment extends Fragment {

    private ListView listView;
    private MaterialButton btnCreateAlliance;
    private SocialUserAdapter adapter;
    private AllianceCreationViewModel viewModel;

    // Fragment owns selection state (IDs)
    private final Set<String> selectedFriendIds = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupAdapter();
        setupViewModel();
        observeViewModel();
        setupListeners();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.loadFriends(currentUserId);
    }

    private void initializeViews(View view) {
        listView = view.findViewById(R.id.friendsListView);
        btnCreateAlliance = view.findViewById(R.id.btn_create_alliance);
    }

    private void setupAdapter() {
        adapter = new SocialUserAdapter(
                requireContext(),
                new ArrayList<>(),
                SocialUserAdapter.Mode.ALLIANCE_SELECT,
                new SocialUserAdapter.ActionCallback() {
                    @Override public void onViewProfile(User user) { /* not used here */ }
                    @Override public void onRemoveFriend(User user) { /* not used here */ }
                    @Override public void onAddFriend(User user) { /* not used here */ }
                    @Override public void onSelectForAlliance(User user) { /* unused */ }
                }
        );
        // initially no selections
        adapter.setSelectedIds(selectedFriendIds);
        listView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AllianceCreationViewModel.class);
    }

    private void observeViewModel() {
        viewModel.getFriendsLiveData().observe(getViewLifecycleOwner(), friends -> {
            adapter.clear();
            if (friends != null) adapter.addAll(friends);
            adapter.notifyDataSetChanged();
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), e -> {
            if (e != null) Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        viewModel.getAllianceCreatedLiveData().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Alliance created!", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });
    }

    private void setupListeners() {
        // Toggle selection on item click
        listView.setOnItemClickListener((AdapterView<?> parent, View itemView, int position, long id) -> {
            User user = adapter.getItem(position);
            if (user == null) return;

            // Toggle ID in selection set
            if (selectedFriendIds.contains(user.getId())) {
                selectedFriendIds.remove(user.getId());
            } else {
                selectedFriendIds.add(user.getId());
            }

            // Tell adapter which IDs are selected so it can redraw (keeps adapter stateless)
            adapter.setSelectedIds(selectedFriendIds);
        });

        btnCreateAlliance.setOnClickListener(v -> {
            TextInputEditText nameInput = requireView().findViewById(R.id.alliance_name);
            String allianceName = nameInput != null && nameInput.getText() != null ? nameInput.getText().toString().trim() : "";

            if (allianceName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter alliance name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedFriendIds.isEmpty()) {
                Toast.makeText(getContext(), "Select at least one friend", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert selected IDs set to list and call viewModel
            List<String> inviteIds = new ArrayList<>(selectedFriendIds);
            viewModel.createAlliance(allianceName, inviteIds);
        });
    }
}
