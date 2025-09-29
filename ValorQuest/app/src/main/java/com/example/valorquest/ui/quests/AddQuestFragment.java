package com.example.valorquest.ui.quests;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
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
    private TextInputEditText etName, etDescription, etTime, etDueDate, etStartDate, etEndDate, etInterval;
    private MaterialAutoCompleteTextView difficultySelect, importanceSelect, categorySelect;
    private TextInputLayout tilName, tilDescription, tilDifficulty, tilImportance, tilCategory,
            tilTime, tilDueDate, tilStartDate, tilEndDate, tilInterval;

    private CheckBox cbAdvanced;
    private boolean isEditMode;

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

        etName = root.findViewById(R.id.et_quest_name);
        tilName = root.findViewById(R.id.til_quest_name);

        etDescription = root.findViewById(R.id.et_description);
        tilDescription = root.findViewById(R.id.til_quest_description);

        difficultySelect = root.findViewById(R.id.act_difficulty_select);
        importanceSelect = root.findViewById(R.id.act_importance_select);
        categorySelect = root.findViewById(R.id.act_category_select);

        tilDifficulty = root.findViewById(R.id.til_difficulty_select);
        tilImportance = root.findViewById(R.id.til_importance_select);
        tilCategory = root.findViewById(R.id.til_category_select);

        cbAdvanced = root.findViewById(R.id.cb_enable_advanced);
        View sectionAdvanced = root.findViewById(R.id.section_advanced);

        etTime = root.findViewById(R.id.et_time_select);
        tilTime = root.findViewById(R.id.til_time_select);

        etDueDate = root.findViewById(R.id.et_due_date);
        tilDueDate = root.findViewById(R.id.til_due_date);

        etStartDate = root.findViewById(R.id.et_start_date);
        tilStartDate = root.findViewById(R.id.til_start_date);

        etEndDate = root.findViewById(R.id.et_end_date);
        tilEndDate = root.findViewById(R.id.til_end_date);

        MaterialButton btnNext = root.findViewById(R.id.btn_add_quest);

        etInterval = root.findViewById(R.id.et_repeating_interval);
        tilInterval = root.findViewById(R.id.til_repeating_interval);

        MaterialAutoCompleteTextView unitView = root.findViewById(R.id.et_repeating_unit);
        unitView.setSimpleItems(new String[]{"Days", "Weeks"});

        unitView.setText("Days", false);

        difficultySelect.setSimpleItems(Arrays.asList("Novice", "Adventurer", "Veteran", "Legendary").toArray(new String[0]));
        importanceSelect.setSimpleItems(Arrays.asList("Low", "Medium", "High", "Special").toArray(new String[0]));

        cbAdvanced.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sectionAdvanced.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            tilDueDate.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });
        sectionAdvanced.setVisibility(cbAdvanced.isChecked() ? View.VISIBLE : View.GONE);
        tilDueDate.setVisibility(cbAdvanced.isChecked() ? View.GONE : View.VISIBLE);

        etTime.setOnClickListener(v -> showTimePicker(selectedHours,selectedMins, etTime::setText));
        etDueDate.setOnClickListener(v -> showDatePicker(etDueDate::setText));
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate::setText));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate::setText));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        int questExecutionId = getArguments() != null
                ? getArguments().getInt("questExecutionId") : -1;

        isEditMode = questExecutionId != -1;

        if (isEditMode) {
            tilDueDate.setVisibility(View.GONE);
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

            if (!validateQuestInputs()) {
                return;
            }

            if(selectedCategoryId == -1 && !isEditMode){
                Toast.makeText(requireContext(), "Please choose a category!", Toast.LENGTH_SHORT).show();
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
//                viewModel.addQuest(dto).observe(getViewLifecycleOwner(), result -> {
//                    if (result.getStatus() == Result.Status.SUCCESS) {
//                        Toast.makeText(requireContext(), result.getData(), Toast.LENGTH_SHORT).show();
//                        Navigation.findNavController(v).popBackStack();
//                    } else if (result.getStatus() == Result.Status.ERROR) {
//                        Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
                viewModel.getUserById(dto.userId).observe(getViewLifecycleOwner(), fullUser -> {
                    if (fullUser != null) {
                        dto.userLevel = fullUser.getLevel();

                        viewModel.addQuest(dto).observe(getViewLifecycleOwner(), result -> {
                            if (result.getStatus() == Result.Status.SUCCESS) {
                                Toast.makeText(requireContext(), result.getData(), Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(v).popBackStack();
                            } else if (result.getStatus() == Result.Status.ERROR) {
                                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private interface DateCallback { void onDate(String formattedDate); }

    private void showDatePicker(DateCallback cb) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 0);
        long tomorrow = calendar.getTimeInMillis();

        Calendar maxCal = Calendar.getInstance();
        maxCal.add(Calendar.YEAR, 1);
        long maxDate = maxCal.getTimeInMillis();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(tomorrow))
                .setEnd(maxDate)
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Select date")
                .setSelection(tomorrow)
                .setCalendarConstraints(constraints)
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

    private boolean validateQuestInputs() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String description = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();
        String time = etTime.getText() == null ? "" : etTime.getText().toString().trim();

        boolean isValid = true;
        boolean isRepeating = cbAdvanced.isChecked();

        // Reset previous errors
        tilName.setError(null);
        tilDescription.setError(null);
        tilTime.setError(null);

        // name
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Please enter a quest name");
            isValid = false;
        } else if (name.length() > 15) {
            tilName.setError("Quest name must be at most 15 characters");
            isValid = false;
        } else if (!name.matches("[a-zA-Z0-9 ]+")) {
            tilName.setError("Only letters and numbers allowed");
            isValid = false;
        }

        // description
        if (!TextUtils.isEmpty(description)) {
            if (description.length() > 40) {
                tilDescription.setError("Description must be at most 40 characters");
                isValid = false;
            } else if (!description.matches("[a-zA-Z0-9 .!?,]+")) {
                tilDescription.setError("Only letters, numbers, spaces and . ! ? , allowed");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(time)) {
            tilTime.setError("Please enter quest time");
            isValid = false;
        }

        if (!isEditMode) {
            if (TextUtils.isEmpty(difficultySelect.getText())) {
                tilDifficulty.setError("Please select a difficulty");
                isValid = false;
            } else {
                tilDifficulty.setError(null);
            }

            if (TextUtils.isEmpty(importanceSelect.getText())) {
                tilImportance.setError("Please select importance");
                isValid = false;
            } else {
                tilImportance.setError(null);
            }

            if (selectedCategoryId == -1) {
                tilCategory.setError("Please select a category");
                isValid = false;
            } else {
                tilCategory.setError(null);
            }

            if(!isRepeating){
                String dueDate = etDueDate.getText() == null ? "" : etDueDate.getText().toString().trim();

                if(dueDate.isEmpty()){
                    tilDueDate.setError("Please enter a date");
                    isValid = false;
                }
            }else{
                String startDate = etStartDate.getText() == null ? "" : etStartDate.getText().toString().trim();
                String endDate = etEndDate.getText() == null ? "" : etEndDate.getText().toString().trim();
                String intervalStr = etInterval.getText().toString().trim();

                if(startDate.isEmpty()){
                    tilStartDate.setError("Please enter a date");
                    isValid = false;
                }

                if(endDate.isEmpty()){
                    tilEndDate.setError("Please enter a date");
                    isValid = false;
                }

                if (TextUtils.isEmpty(intervalStr)) {
                    tilInterval.setError("Interval is required");
                    isValid = false;
                } else if (!intervalStr.matches("\\d+")) {
                    tilInterval.setError("Interval must be a number");
                    isValid = false;
                }
            }
        }

        return isValid;
    }
}