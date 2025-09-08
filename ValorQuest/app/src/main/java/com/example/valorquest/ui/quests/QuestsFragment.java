package com.example.valorquest.ui.quests;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.valorquest.R;

public class QuestsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quests, container, false);
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        View btnAdd = root.findViewById(R.id.btnAddQuest);
        View btnCalendar = root.findViewById(R.id.btnCalendar);
        View btnList = root.findViewById(R.id.btnList);

        if (savedInstanceState == null) {
            showCalendar();
        }

        btnAdd.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_questsFragment_to_addQuestsFragment);
            Toast.makeText(requireContext(), "Add Quest", Toast.LENGTH_SHORT).show();
        });

        btnCalendar.setOnClickListener(v -> showCalendar());
        btnList.setOnClickListener(v -> showList());
    }

    private void showCalendar() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.quests_content_container, new QuestsCalendarFragment(), "calendar")
                .commit();
    }

    private void showList() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.quests_content_container, new QuestsListFragment(), "list")
                .commit();
    }
}