package com.example.valorquest.ui.quests;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.valorquest.R;
import com.example.valorquest.model.Category;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.dto.AddQuestDto;
import com.example.valorquest.model.enums.Difficulty;
import com.example.valorquest.model.enums.Importance;
import com.example.valorquest.model.enums.RepeatingUnit;
import com.example.valorquest.viewmodel.QuestsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddQuestFragment extends Fragment {
    private QuestsViewModel viewModel;
    private int selectedCategoryId = -1;
    private final SimpleDateFormat dateFmt;
    {
        dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFmt.setTimeZone(TimeZone.getDefault());
    }
    private int selectedHours = -1;
    private int selectedMins = -1;
    private AddQuestDto dto;

    public AddQuestFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_quest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(QuestsViewModel.class);
        dto = new AddQuestDto();

        TextInputEditText etName = root.findViewById(R.id.et_name);
        TextInputEditText etDescription = root.findViewById(R.id.et_description);

        MaterialAutoCompleteTextView difficultySelect = root.findViewById(R.id.act_difficulty_select);
        MaterialAutoCompleteTextView importanceSelect = root.findViewById(R.id.act_importance_select);
        MaterialAutoCompleteTextView categorySelect = root.findViewById(R.id.act_category_select);

        CheckBox cbAdvanced = root.findViewById(R.id.cb_enable_advanced);
        View sectionSingleDate = root.findViewById(R.id.section_single_date);
        View sectionAdvanced = root.findViewById(R.id.section_advanced);

        TextInputEditText etTime = root.findViewById(R.id.et_time_select);
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

        etTime.setOnClickListener(v -> showTimePicker(selectedHours,selectedMins,time -> etTime.setText(time)));
        etDueDate.setOnClickListener(v -> showDatePicker(date -> etDueDate.setText(date)));
        etStartDate.setOnClickListener(v -> showDatePicker(date -> etStartDate.setText(date)));
        etEndDate.setOnClickListener(v -> showDatePicker(date -> etEndDate.setText(date)));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        int questExecutionId = getArguments() != null
                ? getArguments().getInt("questExecutionId") : -1;

        boolean isEditMode = questExecutionId != -1;

        if (isEditMode) {
            root.findViewById(R.id.section_single_date).setVisibility(View.GONE);
            root.findViewById(R.id.cb_enable_advanced).setVisibility(View.GONE);
            root.findViewById(R.id.section_advanced).setVisibility(View.GONE);
            root.findViewById(R.id.til_category_select).setVisibility(View.GONE);

            TextView tvTitle = root.findViewById(R.id.tvTitle);
            TextView tvSubtitle = root.findViewById(R.id.tvSubtitle);
            tvTitle.setText("Update Quest");
            tvSubtitle.setText("Change your destiny");

            btnNext.setText("Update");

            viewModel.getDetailedExecutionById(questExecutionId).observe(getViewLifecycleOwner(), quest -> {
                if (quest != null) {
                    selectedHours = quest.date.getHour();
                    selectedMins = quest.date.getMinute();

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    String timeString = quest.date.format(timeFormatter);

                    etName.setText(quest.questName);
                    etDescription.setText(quest.description);
                    difficultySelect.setText(quest.difficulty, false);
                    importanceSelect.setText(quest.importance, false);
                    etTime.setText(timeString);
                }
            });
        }


        if (user != null) {
            String userId = user.getUid();

            viewModel.getCategoriesForUser(userId).observe(
                    getViewLifecycleOwner(), categories -> {
                        if (categories != null && !categories.isEmpty()) {
                            List<String> names = new ArrayList<>();
                            for (Category c : categories) {
                                names.add(c.getName());
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    requireContext(),
                                    android.R.layout.simple_dropdown_item_1line,
                                    names
                            );
                            categorySelect.setAdapter(adapter);

                            categorySelect.setOnItemClickListener((parent, view, position, id) -> {
                                Category selectedCategory = categories.get(position);
                                this.selectedCategoryId = selectedCategory.getId();
                            });
                        }
                    }
            );
        }

        btnNext.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(requireContext(), "You must be logged in", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).popBackStack();
                return;
            }

            if(selectedCategoryId == -1 && !isEditMode){
                Toast.makeText(requireContext(), "Please choose a category first", Toast.LENGTH_SHORT).show();
                return;
            }

            dto.userId = user.getUid();
            dto.name = etName.getText().toString().trim();
            dto.description = etDescription.getText().toString().trim();
            dto.time = etTime.getText().toString().trim();

            try {
                dto.difficulty = Difficulty.valueOf(difficultySelect.getText().toString().toUpperCase(Locale.ROOT));
                dto.importance = Importance.valueOf(importanceSelect.getText().toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                Toast.makeText(requireContext(), "Please choose difficulty and importance first", Toast.LENGTH_SHORT).show();
                return;
            }

            dto.categoryId = selectedCategoryId;
            dto.isRepeating = cbAdvanced.isChecked();

            if (dto.isRepeating) {
                dto.startDate = etStartDate.getText().toString();
                dto.endDate = etEndDate.getText().toString();
                dto.repeatingInterval = Integer.parseInt(etInterval.getText().toString());
                dto.unit = RepeatingUnit.valueOf(unitView.getText().toString().toUpperCase(Locale.ROOT));
            } else {
                dto.dueDate = etDueDate.getText().toString();
            }

            if(isEditMode){
                viewModel.updateQuest(questExecutionId, dto)
                    .observe(getViewLifecycleOwner(), result -> {
                        if (result.getStatus() == Result.Status.SUCCESS) {
                            Toast.makeText(requireContext(), "Quest updated", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(v).popBackStack();
                        } else {
                            Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            }
            else{
                viewModel.addQuest(dto).observe(getViewLifecycleOwner(), result -> {
                    if (result.getStatus() == Result.Status.SUCCESS) {
                        Toast.makeText(requireContext(), result.getData(), Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).popBackStack();
                    } else if (result.getStatus() == Result.Status.ERROR) {
                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
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

    private void showTimePicker(Integer hourInput, Integer minuteInput, OnTimeSelectedListener listener) {
        final Calendar calendar = Calendar.getInstance();

        int hour = (hourInput != -1) ? hourInput : calendar.get(Calendar.HOUR_OF_DAY);
        int minute = (minuteInput != -1) ? minuteInput : calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String formatted = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    listener.onTimeSelected(formatted);
                },
                hour,
                minute,
                true
        );
        timePickerDialog.show();
    }

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }
}