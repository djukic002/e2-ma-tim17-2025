package com.example.valorquest.ui.boss;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.SoundPool;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valorquest.R;
import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class BossRewardFragment extends Fragment {
    private ImageView ivChest, ivGold, ivEquipment;
    private TextView tvGoldAmount, tvEquipmentName;
    private MaterialButton btnOpenChest, btnClaimChest;
    private SoundPool soundPool;
    private int chestOpenSound;
    private boolean isOpened = false;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener shakeListener;

    private boolean bossDefeated = false;
    private double gearDropChance = 0.2;
    private String equipmentName = "Gauntlets of the Titan";
    private int goldReward = 200;

    private boolean hasGold = false;
    private boolean hasEquipment = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boss_reward, container, false);

        if (getArguments() != null) {
            bossDefeated = getArguments().getBoolean("bossDefeated", false);
        }

        if (!bossDefeated) {
            gearDropChance = 0.1;
            goldReward = goldReward / 2;
        }

        // setovati ovde kad se povuce reward iz baze
        ivGold = view.findViewById(R.id.ivGold);
        ivEquipment = view.findViewById(R.id.ivEquipment);

        tvGoldAmount = view.findViewById(R.id.tvGoldAmount);
        tvEquipmentName = view.findViewById(R.id.tvEquipmentName);

        ivChest = view.findViewById(R.id.ivChest);
        btnOpenChest = view.findViewById(R.id.btnOpenChest);
        btnClaimChest = view.findViewById(R.id.btnClaimChest);

        soundPool = new SoundPool.Builder().setMaxStreams(3).build();
        chestOpenSound = soundPool.load(requireContext(), R.raw.chest_open, 1);

        btnOpenChest.setOnClickListener(v -> openChest());
        btnClaimChest.setOnClickListener(v -> claimChest());

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            shakeListener = createShakeListener();
        }

        return view;
    }

    private void openChest() {
        if (isOpened) return;
        isOpened = true;

        ivChest.setImageResource(R.drawable.chest_open);
        soundPool.play(chestOpenSound, 1f, 1f, 1, 0, 1f);

        Random rand = new Random();
        hasGold = true;
        hasEquipment = rand.nextDouble() < gearDropChance;

        View goldColumn = getView().findViewById(R.id.goldColumn);
        if (hasGold) {
            goldColumn.setVisibility(View.VISIBLE);
            tvGoldAmount.setText("+" + goldReward);
        } else {
            goldColumn.setVisibility(View.GONE);
        }

        View equipmentColumn = getView().findViewById(R.id.equipmentColumn);
        if (hasEquipment) {
            equipmentColumn.setVisibility(View.VISIBLE);
            tvEquipmentName.setText(equipmentName);
        } else {
            equipmentColumn.setVisibility(View.GONE);
        }

        btnOpenChest.setVisibility(View.GONE);
        btnClaimChest.setVisibility(View.VISIBLE);
    }

    private void claimChest() {
        Toast.makeText(requireContext(), "You claimed the rewards!", Toast.LENGTH_SHORT).show();
        btnClaimChest.setEnabled(false);

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_bossRewardFragment_to_mainMenuFragment);
    }

    private SensorEventListener createShakeListener() {
        return new SensorEventListener() {
            private static final float SHAKE_THRESHOLD = 2.5f;
            private long lastShakeTime = 0;

            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float gX = x / SensorManager.GRAVITY_EARTH;
                float gY = y / SensorManager.GRAVITY_EARTH;
                float gZ = z / SensorManager.GRAVITY_EARTH;

                float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

                if (gForce > SHAKE_THRESHOLD) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastShakeTime > 800) {
                        lastShakeTime = currentTime;
                        if (!isOpened) openChest();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null && shakeListener != null) {
            sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shakeListener != null) {
            sensorManager.unregisterListener(shakeListener);
        }
    }
}
