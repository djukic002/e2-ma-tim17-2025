package com.example.valorquest.ui.quests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.valorquest.R;
import com.example.valorquest.viewmodel.QuestsViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestsListFragment extends Fragment {
    private QuestsViewModel viewModel;
    private QuestArrayAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quests_list, container, false);

        ListView listView = root.findViewById(R.id.list_quests);
        MaterialAutoCompleteTextView filter = root.findViewById(R.id.act_filter);

        adapter = new QuestArrayAdapter(requireContext(), new ArrayList<>(), quest -> {
            System.out.println("Clicked quest: " + quest.questName);
        });
        listView.setAdapter(adapter);

        String[] filterOptions = {"All", "Repeating", "Non repeating"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, filterOptions);
        filter.setAdapter(filterAdapter);

        filter.setText(filterOptions[0], false);

        filter.setOnItemClickListener((parent, view, position, id) -> {
            String selected = filterOptions[position];
            adapter.filterByRepeating(selected);
        });

        viewModel = new ViewModelProvider(this).get(QuestsViewModel.class);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        viewModel.getDetailedQuestExecutionsForUser(user.getUid(), true).observe(getViewLifecycleOwner(), quests -> {
            adapter.setOriginalList(quests);
        });

        return root;
    }
    }
