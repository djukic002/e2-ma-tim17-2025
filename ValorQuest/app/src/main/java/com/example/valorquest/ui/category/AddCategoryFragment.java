package com.example.valorquest.ui.category;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.example.valorquest.R;
import com.example.valorquest.viewmodel.AddCategoryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddCategoryFragment extends Fragment {

    private AddCategoryViewModel viewModel;

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

        TextInputEditText etName = root.findViewById(R.id.et_category_name);
        TextInputLayout tilColor = root.findViewById(R.id.til_color_hex);
        TextInputEditText etColor = root.findViewById(R.id.et_color_hex);
        MaterialCardView colorPreview = root.findViewById(R.id.color_preview);
        MaterialButton btnAdd = root.findViewById(R.id.btn_add_category);

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

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText() == null ? "" : etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedColor = viewModel.getSelectedColorHex();
            if (TextUtils.isEmpty(selectedColor)) {
                Toast.makeText(requireContext(), "Please enter a valid color hex", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save through ViewModel → Repository → Room
            viewModel.addCategory(user.getUid(), name, selectedColor);

            Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).popBackStack();
        });
    }
}