package com.example.valorquest.ui.social;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.model.User;
import com.example.valorquest.viewmodel.FriendListViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class FriendListFragment extends Fragment {
    private FriendListViewModel viewModel;
    private ListView listView;
    private MaterialButton btnCreateAlliance;
    private MaterialButton btnAddFriend;
    private SocialUserAdapter adapter;

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
        btnCreateAlliance = view.findViewById(R.id.btn_create_alliance);
    }

    private void setupListeners(){
        btnAddFriend.setOnClickListener(v -> {
            Log.d("KLIK DUGMETA", "KLIK");
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_friendListFragment_to_addNewFriendFragment);
        });

        btnCreateAlliance.setOnClickListener(v -> {
            Log.d("KLIK DUGMETA", "KLIK");
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_friendListFragment_to_allianceCreationFragment);
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
                        Toast.makeText(requireContext(), "Profile: " + user.getUsername(), Toast.LENGTH_SHORT).show();
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
    }
}