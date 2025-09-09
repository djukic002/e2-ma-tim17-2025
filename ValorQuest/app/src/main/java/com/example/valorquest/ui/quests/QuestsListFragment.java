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

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestsListFragment extends Fragment {
    private QuestsViewModel viewModel;
    private QuestArrayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quests_list, container, false);

        ListView listView = root.findViewById(R.id.list_quests);
        MaterialAutoCompleteTextView filter = root.findViewById(R.id.act_filter);

        adapter = new QuestArrayAdapter(requireContext(), new ArrayList<>(), quest -> {
            System.out.println("Clicked quest: " + quest.quest.getName());
        });
        listView.setAdapter(adapter);

        String[] statuses = {"All", "Active", "Completed", "Failed"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, statuses);
        filter.setAdapter(filterAdapter);

        viewModel = new ViewModelProvider(this).get(QuestsViewModel.class);

        viewModel.getAllQuestsWithExecutions().observe(getViewLifecycleOwner(), quests -> {
            adapter.clear();
            adapter.addAll(quests);
            adapter.notifyDataSetChanged();
        });

        return root;
    }
}
