package com.example.valorquest.ui.mission;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.MissionSummaryDto;
import com.example.valorquest.viewmodel.AllianceMissionViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AllianceMissionFragment extends Fragment {
    private AllianceMissionViewModel viewModel;
    private TextView missionTitle;
    private MaterialButton btnMission;
    private MissionSummaryDto missionSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alliance_mission, container, false);
        missionTitle = view.findViewById(R.id.missionTitle);
        btnMission = view.findViewById(R.id.btnMission);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AllianceMissionViewModel.class);

        loadActiveMission();

        btnMission.setOnClickListener(v -> viewModel.createMission().observe(getViewLifecycleOwner(), mission -> {
            if (mission != null) {
                Toast.makeText(requireContext(), "Mission created!", Toast.LENGTH_SHORT).show();
                loadActiveMission();
            } else {
                Toast.makeText(requireContext(), "Failed to create mission", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void loadActiveMission() {
        viewModel.getActiveMissionSummary().observe(getViewLifecycleOwner(), summary -> {
            missionSummary = summary;

            if (summary == null) {
            } else {
                // TODO: Update other mission details in UI if needed
            }

            handleMissionButton(summary);
        });
    }

    private void handleMissionButton(MissionSummaryDto summary) {
        if (summary == null) {
            viewModel.isCurrentUserLeader().observe(getViewLifecycleOwner(), isLeader -> {
                if (Boolean.TRUE.equals(isLeader)) {

                    btnMission.setVisibility(View.VISIBLE);
                    btnMission.setAlpha(0f);
                    btnMission.animate()
                            .alpha(1f)
                            .setDuration(1000)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                }
            });
        } else {
            btnMission.setVisibility(View.GONE);
        }
    }
}