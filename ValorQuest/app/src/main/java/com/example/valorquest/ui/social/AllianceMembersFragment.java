package com.example.valorquest.ui.social;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.valorquest.viewmodel.AllianceMembersViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllianceMembersFragment extends Fragment {
    private AllianceMembersViewModel viewModel;

    private SocialUserAdapter allianceMembersAdapter;
    private SocialUserAdapter potentialMembersAdapter;

    private ListView membersListView;
    private MaterialCardView newMembersCard;
    private ListView newMembersListView;
    private MaterialButton btnSendInvites;

    private final Set<String> selectedFriendIds = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupViewModel();
        setupAdapters();
        setupObservers();
        setupListeners();

        viewModel.loadAlliance();
    }

    private void initializeViews(View view) {
        membersListView = view.findViewById(R.id.membersListView);
        newMembersCard = view.findViewById(R.id.new_members_card);
        newMembersListView = view.findViewById(R.id.friendsListView);
        btnSendInvites = view.findViewById(R.id.btn_send_invites);

        // Hide the new members card initially; will show only if current user is leader
        newMembersCard.setVisibility(View.GONE);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AllianceMembersViewModel.class);
    }

    private void setupAdapters() {
        allianceMembersAdapter = new SocialUserAdapter(
                requireContext(),
                new ArrayList<>(),
                SocialUserAdapter.Mode.ALLIANCE_MEMBERS,
                new SocialUserAdapter.ActionCallback() {
                    @Override
                    public void onViewProfile(User user) {}
                    @Override
                    public void onRemoveFriend(User friend) {}
                    @Override public void onAddFriend(User user) {}
                    @Override public void onSelectForAlliance(User user) {}
                }
        );
        membersListView.setAdapter(allianceMembersAdapter);

        potentialMembersAdapter = new SocialUserAdapter(
                requireContext(),
                new ArrayList<>(),
                SocialUserAdapter.Mode.ALLIANCE_SELECT,
                new SocialUserAdapter.ActionCallback() {
                    @Override
                    public void onViewProfile(User user) {}
                    @Override
                    public void onRemoveFriend(User friend) {}
                    @Override public void onAddFriend(User user) {}
                    @Override public void onSelectForAlliance(User user) {}
                }
        );
        newMembersListView.setAdapter(potentialMembersAdapter);
    }

    private void setupObservers() {
        viewModel.getAllianceMembersLiveData().observe(getViewLifecycleOwner(), members -> {
            allianceMembersAdapter.clear();
            allianceMembersAdapter.addAll(members);
            allianceMembersAdapter.notifyDataSetChanged();
        });

        viewModel.getAllianceLiveData().observe(getViewLifecycleOwner(), alliance -> {
            if (alliance == null) return;

            allianceMembersAdapter.setLeaderId(alliance.getLeaderId());

            // Show new members card only if current user is leader
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (currentUserId.equals(alliance.getLeaderId())) {
                newMembersCard.setVisibility(View.VISIBLE);
                potentialMembersAdapter.setLeaderId(alliance.getLeaderId());
            }
        });

        viewModel.getPotentialMembersLiveData().observe(getViewLifecycleOwner(), members -> {
            // Update adapter while preserving selectedFriendIds
            potentialMembersAdapter.clear();
            potentialMembersAdapter.addAll(members);
            potentialMembersAdapter.setSelectedIds(selectedFriendIds);
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), e -> {
            if (e != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        // Selection listener for potential members
        newMembersListView.setOnItemClickListener((AdapterView<?> parent, View itemView, int position, long id) -> {
            User user = potentialMembersAdapter.getItem(position);
            if (user == null) return;

            if (selectedFriendIds.contains(user.getId())) {
                selectedFriendIds.remove(user.getId());
            } else {
                selectedFriendIds.add(user.getId());
            }

            potentialMembersAdapter.setSelectedIds(selectedFriendIds);
        });

        btnSendInvites.setOnClickListener(v -> {
            if (selectedFriendIds.isEmpty()) {
                Toast.makeText(getContext(), "Select at least one friend", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.inviteNewMembers(new ArrayList<>(selectedFriendIds));
        });
    }
}
