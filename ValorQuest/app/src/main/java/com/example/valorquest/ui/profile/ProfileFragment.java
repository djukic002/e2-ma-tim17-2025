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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.UserProfileDto;
import com.example.valorquest.viewmodel.UserProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.example.valorquest.service.FriendService;
import com.example.valorquest.utils.RepositoryCallback;

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
        
        // Hide PP/coins for other users
        if (!isOwnProfile) {
            statsContainer.setVisibility(View.GONE);
        }

        // Generate QR for this profile's userId so others can add them
        if (userId != null) {
            generateAndSetQr(userId);
        }
    }

    private void observeViewModel() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), this::renderProfile);
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