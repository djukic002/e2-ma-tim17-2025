package com.example.valorquest.ui.social;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.valorquest.R;
import com.example.valorquest.model.User;
import com.example.valorquest.viewmodel.AddNewFriendViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class AddNewFriendFragment extends Fragment {
    private AddNewFriendViewModel viewModel;
    private ListView listView;
    private AddFriendAdapter adapter;
    private TextInputEditText searchBar;
    private TextView emptyStateText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_new_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.potential_friends_listview);
        searchBar = view.findViewById(R.id.et_username);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        setupAdapter();
        setupViewModel();
        observeViewModel();
        setupSearchBar();

        viewModel.loadUsers(); // initial load
    }

    private void setupAdapter() {
        adapter = new AddFriendAdapter(requireContext(), new ArrayList<>(), new AddFriendAdapter.FriendActionCallback() {
            @Override
            public void onAddFriend(User user) {
                viewModel.addFriend(user,
                        () -> {
                            adapter.remove(user);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(requireContext(),
                                    user.getUsername() + " added!", Toast.LENGTH_SHORT).show();
                        },
                        () -> Toast.makeText(requireContext(),
                                "Failed to add " + user.getUsername(),
                                Toast.LENGTH_SHORT).show());
            }
        });
        listView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AddNewFriendViewModel.class);
    }

    private void observeViewModel() {
        viewModel.filteredUsers.observe(getViewLifecycleOwner(), users -> {
            adapter.clear();
            if (users != null && !users.isEmpty()) {
                adapter.addAll(users);
                listView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);

                // Differentiate between "no users at all" and "no search results"
                String query = searchBar.getText().toString().trim();
                if (query.isEmpty()) {
                    emptyStateText.setText("The tavern is empty, no comrades can be found...");
                } else {
                    emptyStateText.setText("No adventurers match your spell of search. Try again!");
                }
            }
            adapter.notifyDataSetChanged();
        });


        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(),
                        "Error loading users: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
