package com.example.valorquest.viewmodel;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.example.valorquest.data.repositories.CategoryRepository;
import com.example.valorquest.model.Category;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddCategoryViewModel extends ViewModel {

    private final CategoryRepository repository;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    private String selectedColorHex;

    @Inject
    public AddCategoryViewModel(CategoryRepository repository) {
        this.repository = repository;
    }

    public void setSelectedColorHex(@Nullable String colorHex) {
        this.selectedColorHex = colorHex;
    }

    @Nullable
    public String getSelectedColorHex() {
        return selectedColorHex;
    }

    public String getCurrentUserId(Context context) {
        // TODO: Replace with actual user id from your auth/session
        return "demo_user_id";
    }

    public interface AddCallback {
        void onComplete(Boolean success);
    }

    public void addCategory(Context context, String userId, String name, String colorHex, AddCallback cb) {
        ioExecutor.execute(() -> {
            try {
                Category category = new Category(userId, name, colorHex);
                boolean ok = repository.addCategory(category);
                if (cb != null) cb.onComplete(ok);
            } catch (Exception e) {
                if (cb != null) cb.onComplete(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdown();
    }
}