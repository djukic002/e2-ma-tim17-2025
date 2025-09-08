package com.example.valorquest.ui.quests;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.valorquest.R;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;
import com.example.valorquest.model.enums.Difficulty;
import com.example.valorquest.model.enums.Importance;
import com.example.valorquest.model.enums.RepeatingUnit;
import com.example.valorquest.viewmodel.AddQuestViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddQuestFragment extends Fragment {
    //private AddQuestViewModel viewModel;
    private final SimpleDateFormat dateFmt;
    {
        dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFmt.setTimeZone(TimeZone.getDefault());
    }

    public AddQuestFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_quest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        //viewModel = new ViewModelProvider(this).get(AddQuestViewModel.class);

        TextInputEditText etName = root.findViewById(R.id.et_name);
        TextInputEditText etDescription = root.findViewById(R.id.et_description);

        MaterialAutoCompleteTextView difficultySelect = root.findViewById(R.id.act_difficulty_select);
        MaterialAutoCompleteTextView importanceSelect = root.findViewById(R.id.act_importance_select);
        //MaterialAutoCompleteTextView categorySelect = root.findViewById(R.id.act_category_select);

        CheckBox cbAdvanced = root.findViewById(R.id.cb_enable_advanced);
        View sectionSingleDate = root.findViewById(R.id.section_single_date);
        View sectionAdvanced = root.findViewById(R.id.section_advanced);

        TextInputEditText etDueDate = root.findViewById(R.id.et_due_date);
        TextInputEditText etStartDate = root.findViewById(R.id.et_start_date);
        TextInputEditText etEndDate = root.findViewById(R.id.et_end_date);

        MaterialButton btnNext = root.findViewById(R.id.btn_add_quest);

        TextInputEditText etInterval = root.findViewById(R.id.et_repeating_interval);

        MaterialAutoCompleteTextView unitView = root.findViewById(R.id.et_repeating_unit);
        unitView.setSimpleItems(Arrays.asList("Daily", "Weekly").toArray(new String[0]));

        // Populate selects (replace with your values or resources)
        difficultySelect.setSimpleItems(Arrays.asList("Novice", "Adventurer", "Veteran", "Legendary").toArray(new String[0]));
        importanceSelect.setSimpleItems(Arrays.asList("Low", "Medium", "High", "Special").toArray(new String[0]));

        // Toggle sections
        cbAdvanced.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sectionAdvanced.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            sectionSingleDate.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });
        // Initialize
        sectionAdvanced.setVisibility(cbAdvanced.isChecked() ? View.VISIBLE : View.GONE);
        sectionSingleDate.setVisibility(cbAdvanced.isChecked() ? View.GONE : View.VISIBLE);

        // Date pickers
        etDueDate.setOnClickListener(v -> showDatePicker(date -> etDueDate.setText(date)));
        etStartDate.setOnClickListener(v -> showDatePicker(date -> etStartDate.setText(date)));
        etEndDate.setOnClickListener(v -> showDatePicker(date -> etEndDate.setText(date)));

        // ✅ Button click
        btnNext.setOnClickListener(v -> {
            AddQuestDto dto = new AddQuestDto();
            dto.userId = "123"; // TODO: replace with logged-in user ID
            dto.name = etName.getText().toString().trim();
            dto.description = etDescription.getText().toString().trim();

            // Map difficulty & importance from string → enum
            dto.difficulty = Difficulty.valueOf(difficultySelect.getText().toString().toUpperCase(Locale.ROOT));
            dto.importance = Importance.valueOf(importanceSelect.getText().toString().toUpperCase(Locale.ROOT));

            // TODO: replace with actual category from DB dropdown
            dto.categoryId = 1;

            dto.isRepeating = cbAdvanced.isChecked();

            if (dto.isRepeating) {
                dto.startDate = etStartDate.getText().toString();
                dto.endDate = etEndDate.getText().toString();
                dto.repeatingInterval = Integer.parseInt(etInterval.getText().toString());
                dto.unit = RepeatingUnit.valueOf(unitView.getText().toString().toUpperCase(Locale.ROOT));
            } else {
                dto.dueDate = etDueDate.getText().toString();
            }

            Log.d("AddQuestFragment", dto.toString());

            // 🔥 Call ViewModel
//            viewModel.addQuest(dto).observe(getViewLifecycleOwner(), result -> {
//                if (result.getStatus() == Result.Status.SUCCESS) {
//                    Toast.makeText(requireContext(), result.getData(), Toast.LENGTH_SHORT).show();
//                    Navigation.findNavController(v).popBackStack();
//                } else if (result.getStatus() == Result.Status.ERROR) {
//                    Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
        });
    }

    private interface DateCallback { void onDate(String formattedDate); }

    private void showDatePicker(DateCallback cb) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            try {
                String formatted = dateFmt.format(new Date(selection));
                cb.onDate(formatted);
            } catch (Exception ignored) { }
        });

        picker.show(getParentFragmentManager(), "date_picker");
    }
}