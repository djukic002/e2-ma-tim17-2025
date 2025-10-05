package com.example.valorquest.ui.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.valorquest.R;
import com.example.valorquest.service.FriendService;
import com.example.valorquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QrScanFragment extends Fragment {
    private final FriendService friendService = new FriendService(new com.example.valorquest.data.repositories.UserRepository());
    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null) {
            Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            // Only go back if scan was cancelled
            requireActivity().onBackPressed();
        } else {
            handleScannedPayload(result.getContents());
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startQrScan();
    }

    private void startQrScan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a friend's QR code");
        options.setOrientationLocked(false);
        barcodeLauncher.launch(options);
    }

    private void handleScannedPayload(String payload) {
        if (payload == null) return;
        if (!payload.startsWith("ADD_FRIEND:")) {
            Toast.makeText(requireContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
            return;
        }
        String friendUserId = payload.substring("ADD_FRIEND:".length());
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        if (friendUserId.equals(currentUserId)) {
            Toast.makeText(requireContext(), "Cannot add yourself as friend", Toast.LENGTH_SHORT).show();
            return;
        }
        friendService.addFriendByUserId(friendUserId, new RepositoryCallback<Boolean>() {
            @Override
            public void onComplete(Boolean result) {
                if (result) {
                    Toast.makeText(requireContext(), "Friend added successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to add friend", Toast.LENGTH_SHORT).show();
                }
                // Navigate back after processing the result
                requireActivity().onBackPressed();
            }
        });
    }
}
