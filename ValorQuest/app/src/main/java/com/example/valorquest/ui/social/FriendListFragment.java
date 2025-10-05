package com.example.valorquest.ui.social;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.model.User;
import com.example.valorquest.viewmodel.FriendListViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

public class FriendListFragment extends Fragment {
    private FriendListViewModel viewModel;
    private ListView listView;
    private MaterialButton btnCreateAlliance;
    private MaterialButton btnAddFriend;
    private SocialUserAdapter adapter;
    private FrameLayout allianceContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupAdapter();
        setupViewModel();
        observeViewModel();
        setupListeners();

        // Replace with your current user ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.loadFriends(currentUserId);
    }

    private void initializeViews(View view) {
        listView = view.findViewById(R.id.friendsListView);
        btnAddFriend = view.findViewById(R.id.btn_add_friend);
        allianceContainer = view.findViewById(R.id.alliance_container);
    }

    private void setupListeners(){
        btnAddFriend.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_friendListFragment_to_addNewFriendFragment);
        });
    }

    private void setupAdapter() {
        adapter = new SocialUserAdapter(
                requireContext(),
                new ArrayList<>(),
                SocialUserAdapter.Mode.FRIEND_LIST,
                new SocialUserAdapter.ActionCallback() {
                    @Override
                    public void onViewProfile(User user) {
                        Bundle args = new Bundle();
                        args.putString("userId", user.getId());
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.profileFragment, args);
                    }

                    @Override
                    public void onRemoveFriend(User friend) {
                        viewModel.removeFriend(friend,
                                () -> Toast.makeText(requireContext(),
                                        friend.getUsername() + " has been removed.", Toast.LENGTH_SHORT).show(),
                                () -> Toast.makeText(requireContext(),
                                        "Failed to remove " + friend.getUsername(), Toast.LENGTH_SHORT).show());
                    }

                    @Override public void onAddFriend(User user) {} // not used here
                    @Override public void onSelectForAlliance(User user) {} // not used here
                }
        );
        listView.setAdapter(adapter);
    }


    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FriendListViewModel.class);
    }

    private void observeViewModel() {
        // Observe friends list
        viewModel.getFriendsLiveData().observe(getViewLifecycleOwner(), friends -> {
            adapter.clear();
            adapter.addAll(friends);
            adapter.notifyDataSetChanged();
        });

        // Observe errors
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), e -> {
            if (e != null) {
                Toast.makeText(getContext(),
                        "Error loading friends: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsInAllianceLiveData().observe(getViewLifecycleOwner(), isInAlliance -> {
            allianceContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            if (isInAlliance) {
                View allianceView = inflater.inflate(R.layout.layout_alliance_info, allianceContainer, false);
                TextView txtAllianceName = allianceView.findViewById(R.id.txt_alliance_name);
                TextView txtLeader = allianceView.findViewById(R.id.txt_alliance_leader);

                viewModel.getAllianceLiveData().observe(getViewLifecycleOwner(), alliance -> {
                    if (alliance != null) txtAllianceName.setText(alliance.getName());
                });
                viewModel.getLeaderLiveData().observe(getViewLifecycleOwner(), leader -> {
                    if (leader != null) txtLeader.setText("Leader: " + leader.getUsername());
                });

                allianceContainer.addView(allianceView);

                ImageButton btnAllianceLeave = allianceView.findViewById(R.id.btn_alliance_leave);
                btnAllianceLeave.setOnClickListener(v -> showLeaveDisbandConfirmationDialog());
                ImageButton btnAllianceMembers = allianceView.findViewById(R.id.btn_alliance_members);
                btnAllianceMembers.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_friendListFragment_to_allianceMembersFragment);
                });
            } else {
                View emptyView = inflater.inflate(R.layout.layout_alliance_empty, allianceContainer, false);
                allianceContainer.addView(emptyView);
                btnCreateAlliance = allianceContainer.findViewById(R.id.btn_create_alliance);
                btnCreateAlliance.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_friendListFragment_to_allianceCreationFragment);
                });
            }
        });

        viewModel.checkAllianceStatus();

        viewModel.getActionCompleted().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Action completed successfully.", Toast.LENGTH_SHORT).show();
                // Update UI accordingly, e.g., reload alliance panel
            } else {
                Toast.makeText(requireContext(), "Action failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLeaveDisbandConfirmationDialog() {
        LiveData<User> leader = viewModel.getLeaderLiveData();
        boolean isLeader = Objects.requireNonNull(leader.getValue()).getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        String title = isLeader ? "Disband Alliance?" : "Leave Alliance?";
        String message = isLeader ?
                "You are the leader. Disbanding the alliance will remove all members. Continue?" :
                "Are you sure you want to leave the alliance?";

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(isLeader ? "Disband" : "Leave", (dialog, which) -> {
                    if (isLeader)
                        viewModel.disbandAlliance();
                    else
                        viewModel.leaveAlliance();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}