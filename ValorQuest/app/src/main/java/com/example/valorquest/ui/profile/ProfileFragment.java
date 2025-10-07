package com.example.valorquest.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.UserProfileDto;
import com.example.valorquest.model.dto.UserItemDTO;
import com.example.valorquest.viewmodel.UserProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.example.valorquest.service.FriendService;
import com.example.valorquest.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private UserProfileViewModel viewModel;

    private ImageView avatarImage;
    private TextView usernameText;
    private TextView titleText;
    private TextView ppText;
    private TextView coinsText;
    private TextView levelText;
    private ProgressBar xpBar;
    private TextView xpText;
    private View statsContainer;
    private ImageView qrImage;
    private ListView inventoryList;
    private ListView equippedList;
    private InventoryAdapter inventoryAdapter;
    private EquippedAdapter equippedAdapter;
    private MaterialButton btnChangePassword;
    private MaterialButton btnStatistics;
    private final FriendService friendService = new FriendService(new com.example.valorquest.data.repositories.UserRepository());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        avatarImage = view.findViewById(R.id.profile_avatar);
        usernameText = view.findViewById(R.id.profile_username);
        titleText = view.findViewById(R.id.profile_title);
        ppText = view.findViewById(R.id.profile_pp);
        coinsText = view.findViewById(R.id.profile_coins);
        levelText = view.findViewById(R.id.profile_level);
        xpBar = view.findViewById(R.id.profile_xp_bar);
        xpText = view.findViewById(R.id.profile_xp_text);
        statsContainer = view.findViewById(R.id.profile_stats_container);
        qrImage = view.findViewById(R.id.profile_qr_image);
        inventoryList = view.findViewById(R.id.inventory_list);
        equippedList = view.findViewById(R.id.equipped_list);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnStatistics = view.findViewById(R.id.btn_statistics);

        // Initialize adapters
        inventoryAdapter = new InventoryAdapter(requireContext(), new ArrayList<>());
        equippedAdapter = new EquippedAdapter(requireContext(), new ArrayList<>());
        
        inventoryList.setAdapter(inventoryAdapter);
        equippedList.setAdapter(equippedAdapter);

        observeViewModel();

        String userId = null;
        Bundle args = getArguments();
        if (args != null && args.containsKey("userId")) {
            userId = args.getString("userId");
        }
        if (userId == null || userId.isEmpty()) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }
        if (userId != null) {
            viewModel.loadProfile(userId);
        }
        
        // Check if viewing own profile or other user's profile
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        boolean isOwnProfile = userId != null && userId.equals(currentUserId);
        
        // Hide PP/coins, inventory, and buttons for other users
        if (!isOwnProfile) {
            statsContainer.setVisibility(View.GONE);
            // Hide inventory card for other users
            View inventoryCard = view.findViewById(R.id.inventory_card);
            if (inventoryCard != null) {
                inventoryCard.setVisibility(View.GONE);
            }
            // Hide change password and statistics buttons for other users
            btnChangePassword.setVisibility(View.GONE);
            btnStatistics.setVisibility(View.GONE);
        }

        // Generate QR for this profile's userId so others can add them
        if (userId != null) {
            generateAndSetQr(userId);
        }
        
        // Load equipment only for own profile
        if (isOwnProfile && userId != null) {
            viewModel.loadEquipment(userId);
        }
        
        // Setup button listeners
        setupButtonListeners();
    }

    private void observeViewModel() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), this::renderProfile);
        
        // Observe equipment data
        viewModel.getInventoryEquipment().observe(getViewLifecycleOwner(), this::updateInventory);
        viewModel.getEquippedEquipment().observe(getViewLifecycleOwner(), this::updateEquipped);
        viewModel.getEquipmentMessage().observe(getViewLifecycleOwner(), this::showEquipmentMessage);
        
        // Set up adapter listeners
        setupAdapterListeners();
    }
    
    private void setupAdapterListeners() {
        inventoryAdapter.setActivateListener(userItemId -> {
            viewModel.activateEquipment(userItemId);
        });
        
        equippedAdapter.setUpgradeListener(userItemId -> {
            viewModel.upgradeWeapon(userItemId);
        });
    }
    
    private void updateInventory(List<UserItemDTO> items) {
        android.util.Log.d("ProfileFragment", "Updating inventory with " + (items != null ? items.size() : 0) + " items");
        inventoryAdapter.clear();
        if (items != null) {
            inventoryAdapter.addAll(items);
        }
        inventoryAdapter.notifyDataSetChanged();
    }
    
    private void updateEquipped(List<UserItemDTO> items) {
        android.util.Log.d("ProfileFragment", "Updating equipped with " + (items != null ? items.size() : 0) + " items");
        equippedAdapter.clear();
        if (items != null) {
            equippedAdapter.addAll(items);
        }
        equippedAdapter.notifyDataSetChanged();
    }
    
    private void showEquipmentMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupButtonListeners() {
        btnChangePassword.setOnClickListener(v -> {
            // Navigate to change password fragment
            androidx.navigation.Navigation.findNavController(requireView())
                .navigate(R.id.action_profileFragment_to_changePasswordFragment);
        });
        
        btnStatistics.setOnClickListener(v -> {
            // TODO: Navigate to statistics fragment when implemented
            Toast.makeText(requireContext(), "Statistics feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void renderProfile(UserProfileDto dto) {
        if (dto == null) return;

        int avatarResId = requireContext().getResources().getIdentifier("avatar_" + dto.getAvatarId(), "drawable", requireContext().getPackageName());
        if (avatarResId != 0) {
            avatarImage.setImageResource(avatarResId);
        }
        usernameText.setText(dto.getUsername());
        titleText.setText(dto.getTitle());
        ppText.setText(String.valueOf(dto.getBasePP()));
        coinsText.setText(String.valueOf(dto.getCoins()));
        levelText.setText("Lv. " + dto.getLevel());

        xpBar.setMax(dto.getRequiredXPForNextLevel());
        xpBar.setProgress(dto.getXP());
        xpText.setText(dto.getXP() + "/" + dto.getRequiredXPForNextLevel());
    }

    private void generateAndSetQr(String targetUserId) {
        try {
            String payload = "ADD_FRIEND:" + targetUserId;
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(payload, BarcodeFormat.QR_CODE, 600, 600);
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            // ignore for now
        }
    }
}