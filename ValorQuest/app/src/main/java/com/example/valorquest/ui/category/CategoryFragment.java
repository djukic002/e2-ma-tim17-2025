package com.example.valorquest.ui.category;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.valorquest.R;
import com.example.valorquest.viewmodel.CategoryViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryFragment extends Fragment {
    private CategoryViewModel viewModel;
    private CategoryArrayAdapter adapter;

    public CategoryFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        View addBtn = root.findViewById(R.id.btn_go_add_category);
        if (addBtn != null) {
            addBtn.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.action_categoryFragment_to_addCategoryFragment));
        }

        ListView lvCategories = root.findViewById(R.id.lvCategories);

        adapter = new CategoryArrayAdapter(requireContext(), new ArrayList<>(), category -> {
            // Placeholder: Edit button does nothing for now
        });

        lvCategories.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            viewModel.getCategoriesForUser(user.getUid()).observe(
                    getViewLifecycleOwner(),
                    categories -> {
                        adapter.clear();
                        adapter.addAll(categories);
                        adapter.notifyDataSetChanged();
                    }
            );
        }
    }
}