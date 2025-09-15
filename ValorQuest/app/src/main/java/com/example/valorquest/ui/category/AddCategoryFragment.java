package com.example.valorquest.ui.category;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.model.Result;
import com.example.valorquest.viewmodel.AddCategoryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.AndroidEntryPoint;
import yuku.ambilwarna.AmbilWarnaDialog;

@AndroidEntryPoint
public class AddCategoryFragment extends Fragment {

    private AddCategoryViewModel viewModel;

    // edit mode args
    private int argCategoryId = -1;
    private String argUserId = "";
    private String argName = "";
    private String argColor = "";

    public AddCategoryFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AddCategoryViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            argCategoryId = args.getInt("categoryId", -1);
            argUserId = args.getString("userId", "");
            argName = args.getString("name", "");
            argColor = args.getString("color", "");
        }
        final boolean isEdit = argCategoryId != -1;

        TextInputEditText etName = root.findViewById(R.id.et_category_name);
        TextInputLayout tilColor = root.findViewById(R.id.til_color_hex);
        TextInputEditText etColor = root.findViewById(R.id.et_color_hex);
        MaterialCardView colorPreview = root.findViewById(R.id.color_preview);
        MaterialButton btnAdd = root.findViewById(R.id.btn_add_category);
        TextView title = root.findViewById(R.id.tvTitle);

        // Prefill for edit mode
        if (isEdit) {
            title.setText("Edit Category");
            etName.setText(argName);
            etName.setEnabled(false); // only editing color; enable if you also want name edits
            if (!TextUtils.isEmpty(argColor)) etColor.setText(argColor);
        } else {
            title.setText("Add Category");
        }


        Runnable applyColor = () -> {
            String hex = etColor.getText() == null ? "" : etColor.getText().toString().trim();
            if (hex.isEmpty()) {
                tilColor.setError(null);
                viewModel.setSelectedColorHex(null);
                return;
            }
            try {
                int parsed = Color.parseColor(hex);
                colorPreview.setCardBackgroundColor(parsed);
                tilColor.setError(null);
                viewModel.setSelectedColorHex(hex);
            } catch (IllegalArgumentException ex) {
                tilColor.setError("Invalid hex (e.g. #FF9800)");
                viewModel.setSelectedColorHex(null);
            }
        };

        applyColor.run();

        etColor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { applyColor.run(); }
        });

        colorPreview.setOnClickListener(v -> openColorPicker(etColor, colorPreview, applyColor));

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText() == null ? "" : etName.getText().toString().trim();
            String selectedColor = viewModel.getSelectedColorHex();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(selectedColor)) {
                Toast.makeText(requireContext(), "Please enter a valid color hex", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit) {
                viewModel.changeCategoryColor(argCategoryId, user.getUid()).observe(
                        getViewLifecycleOwner(), result -> {
                            if (result.getStatus() == Result.Status.SUCCESS) {
                                Toast.makeText(requireContext(), result.getData(), Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(v).popBackStack();
                            } else if (result.getStatus() == Result.Status.ERROR) {
                                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } else {
                viewModel.addCategory(user.getUid(), name).observe(
                        getViewLifecycleOwner(), result -> {
                            if (result.getStatus() == Result.Status.SUCCESS) {
                                Toast.makeText(requireContext(), result.getData(), Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(v).popBackStack();
                            } else if (result.getStatus() == Result.Status.ERROR) {
                                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });
    }

    private void openColorPicker(TextInputEditText etColor, MaterialCardView preview, Runnable applyColor) {
        int initialColor;
        try {
            initialColor = Color.parseColor(etColor.getText().toString().trim());
        } catch (Exception e) {
            initialColor = Color.parseColor("#FF9800");
        }

        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(requireContext(), initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                preview.setCardBackgroundColor(color);
                String hex = String.format("#%06X", (0xFFFFFF & color));
                etColor.setText(hex);
                applyColor.run();
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }
        });

        colorPicker.show();

        AlertDialog dialog = colorPicker.getDialog();
        if (dialog != null && dialog.getWindow() != null) {

            int width = (int) (300 * getResources().getDisplayMetrics().density);
            int height = (int) (370 * getResources().getDisplayMetrics().density);
            dialog.getWindow().setLayout(width, height);

            Drawable imageBg = ContextCompat.getDrawable(requireContext(), R.drawable.card_background);

            dialog.getWindow().setBackgroundDrawable(imageBg);

            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

            if (positiveButton != null) styleDialogButton(positiveButton);
            if (negativeButton != null) styleDialogButton(negativeButton);
        }
    }

        private void styleDialogButton(Button button) {
            button.setTextSize(24);
            button.setAllCaps(false);
            button.setTextColor(Color.parseColor("#8B1E1E"));
            button.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.main_font_family), Typeface.BOLD);
        }
}