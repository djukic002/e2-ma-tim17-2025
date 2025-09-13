package com.example.valorquest.ui.quests;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.DetailedQuestExecutionDto;
import com.example.valorquest.viewmodel.QuestsViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetailedQuestFragment extends Fragment {
    public static final String ARG_EXECUTION_ID = "execution_id";
    private TextView tvTitle, tvDescription, tvStatus, tvDifficulty, tvImportance,
            tvDate, tvRepeating, tvCategoryName, btnEdit, btnDelete;
    private MaterialButton btnComplete, btnCancel, btnPause, btnActivate;
    private View categoryColorView;
    private QuestsViewModel viewModel;
    private DetailedQuestExecutionDto quest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailed_quest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tvQuestTitle);
        tvDescription = view.findViewById(R.id.tvQuestDescription);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvDifficulty = view.findViewById(R.id.tvDifficulty);
        tvImportance = view.findViewById(R.id.tvImportance);
        tvDate = view.findViewById(R.id.tvDate);
        tvRepeating = view.findViewById(R.id.tvRepeating);
        tvCategoryName = view.findViewById(R.id.tvCategoryName);
        categoryColorView = view.findViewById(R.id.viewCategoryColor);

        btnComplete = view.findViewById(R.id.btnComplete);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnPause = view.findViewById(R.id.btnPause);
        btnActivate = view.findViewById(R.id.btnActivate);

        btnEdit = view.findViewById(R.id.btnEditQuest);
        btnDelete = view.findViewById(R.id.btnDeleteQuest);

        viewModel = new ViewModelProvider(this).get(QuestsViewModel.class);

        int executionId = getArguments().getInt(ARG_EXECUTION_ID);

        viewModel.getDetailedExecutionById(executionId).observe(getViewLifecycleOwner(), quest -> {
            if (quest != null) {
                this.quest = quest;
                populateFields();
                updateButtonBar();
            }
        });

        btnEdit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("questExecutionId", quest.executionId);
            Navigation.findNavController(v).navigate(
                    R.id.action_detailedQuestFragment_to_addQuestsFragment,
                    args
            );
        });

        String repeatingAlert = "Are you sure you want to delete this quest? This will delete future repeating quests.";
        String nonRepAlert = "Are you sure you want to delete this quest? This action cannot be undone.";

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Quest")
                    .setMessage(quest.isRepeating ? repeatingAlert : nonRepAlert)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        viewModel.deleteQuest(quest.questId, quest.executionId).observe(getViewLifecycleOwner(), result -> {
                            if (result.getStatus() == Result.Status.SUCCESS) {
                                Toast.makeText(getContext(), result.getData(), Toast.LENGTH_SHORT).show();

                                NavController navController = NavHostFragment.findNavController(this);
                                navController.popBackStack();
                            } else if (result.getStatus() == Result.Status.ERROR) {
                                Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void populateFields() {
        tvTitle.setText(quest.questName);
        tvDescription.setText(quest.description);
        tvStatus.setText(quest.status);
        tvDifficulty.setText(quest.difficulty);
        tvImportance.setText(quest.importance);
        tvDate.setText(quest.date.toString());
        tvRepeating.setText((quest.isRepeating ? "Yes" : "No"));
        tvCategoryName.setText(quest.categoryName);

        try {
            categoryColorView.getBackground().setTint(Color.parseColor(quest.categoryColor));
        } catch (IllegalArgumentException e) {
            categoryColorView.getBackground().setTint(Color.GRAY);
        }
    }

    private void updateButtonBar() {
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnActivate.setVisibility(View.GONE);
        btnEdit.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);

        if ("active".equalsIgnoreCase(quest.status)) {
            btnComplete.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.VISIBLE);

            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else if ("paused".equalsIgnoreCase(quest.status)) {
            btnActivate.setVisibility(View.VISIBLE);

            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        }
    }
}