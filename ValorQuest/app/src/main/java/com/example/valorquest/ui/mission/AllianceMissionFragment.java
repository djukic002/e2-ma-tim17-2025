package com.example.valorquest.ui.mission;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.MissionSummaryDto;
import com.example.valorquest.model.dto.UserContributionDto;
import com.example.valorquest.viewmodel.AllianceMissionViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AllianceMissionFragment extends Fragment {
    private AllianceMissionViewModel viewModel;
    private TextView missionTitle, tvBossHpText;
    private MaterialButton btnMission;
    private MissionSummaryDto missionSummary;

    private ConstraintLayout missionContainer;
    private TextView tvNoMission;

    private ProgressBar progressBossHp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alliance_mission, container, false);
        missionTitle = view.findViewById(R.id.missionTitle);
        btnMission = view.findViewById(R.id.btnMission);
        progressBossHp = view.findViewById(R.id.progressBossHp);
        tvBossHpText = view.findViewById(R.id.tvBossHpText);
        missionContainer = view.findViewById(R.id.missionContainer);
        tvNoMission = view.findViewById(R.id.tvNoMission);

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

            if (summary != null) {
                missionContainer.setVisibility(View.VISIBLE);
                tvNoMission.setVisibility(View.GONE);

                int originalHp = missionSummary.originalBossHp;
                int currentHp = missionSummary.currentBossHp;
                int progress = (int) ((currentHp / (float) originalHp) * 100);
                progressBossHp.setMax(100);
                progressBossHp.setProgress(progress);

                tvBossHpText.setText(currentHp + " / " + originalHp);

                displayUserContributions();
            }
            else{
                missionContainer.setVisibility(View.GONE);
                tvNoMission.setVisibility(View.VISIBLE);
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
    private void displayUserContributions() {
        LinearLayout container = requireView().findViewById(R.id.missionDetailsContainer);
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (UserContributionDto contribution : missionSummary.contributions) {
            View itemView = inflater.inflate(R.layout.item_user_contribution, container, false);

            TextView tvUsername = itemView.findViewById(R.id.tvUsername);
            ProgressBar pbContribution = itemView.findViewById(R.id.pbUserContribution);
            TextView tvValue = itemView.findViewById(R.id.tvContributionValue);

            tvUsername.setText(contribution.username);

            int maxHp = missionSummary.originalBossHp;
            int current = contribution.totalHpDealt;

            pbContribution.setMax(maxHp);
            pbContribution.setProgress(current);

            tvValue.setText(current + " / " + maxHp);

            container.addView(itemView);
        }
    }
}