package com.example.valorquest.ui.boss;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.valorquest.R;
import com.example.valorquest.model.Boss;
import com.example.valorquest.model.Quest;
import com.example.valorquest.model.Result;
import com.example.valorquest.model.User;
import com.example.valorquest.model.UserItem;
import com.example.valorquest.model.dto.UserItemWithEquipmentDto;
import com.example.valorquest.model.enums.BossStatus;
import com.example.valorquest.viewmodel.BossFightViewmodel;
import com.example.valorquest.viewmodel.QuestsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BossFightFragment extends Fragment {
    private BossFightViewmodel bossViewmodel;
    private Boss boss;
    private int pp;
    private double hitChance;
    private int goldReward;
    private MediaPlayer bgMusicPlayer;
    private VideoView bossVideo;
    private TextView tvHpValue, tvPpValue, tvAttackCountValue, tvHitChanceValue;
    private MaterialButton btnBoss;
    private SoundPool soundPool;
    private int backoffSound, deathSound, notWelcomeSound, smashSound, swordSound, laughSound ,grunt1Sound, grunt2Sound, grunt3Sound, pathethicSound;
    private enum Sounds{BACKOFF, DEATH, NOT_WELCOME, SMASH, SWORD, GRUNT, LAUGH, PATHETHIC};
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener shakeListener;
    private boolean shakeEnabled;
    private NavController navController;

    public BossFightFragment() {
    }
    private void loadActiveBoss(String userId) {
        bossViewmodel.getActiveBossForUser(userId).observe(getViewLifecycleOwner(), result -> {
            if (!isAdded()) return; // fragment might be detached

            if (result.getStatus() == Result.Status.SUCCESS) {
                boss = result.getData();

                if (boss == null) {
                    Toast.makeText(requireContext(), "No active boss. Returning to main menu.", Toast.LENGTH_SHORT).show();
                    navController.popBackStack(R.id.mainMenuFragment, false);
                } else {
                    this.hitChance = boss.getHitChance();
                    this.goldReward = boss.getGoldReward();

                    loadUserItems(userId);
                }
            } else {
                Toast.makeText(requireContext(), "Failed to load boss: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                navController.popBackStack(R.id.mainMenuFragment, false);
            }
        });
    }

    private void loadCurrentUser(String userId) {
        bossViewmodel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (!isAdded()) return;

            if (user != null) {
                this.pp = user.getBasePP();
            } else {
                Toast.makeText(requireContext(), "Failed to load user.", Toast.LENGTH_SHORT).show();
                navController.popBackStack(R.id.mainMenuFragment, false);
            }
        });
    }
    private void loadUserItems(String userId) {
        bossViewmodel.getUserItemsWithEquipmentLiveData(userId)
                .observe(getViewLifecycleOwner(), userItems -> {
                    if (!isAdded()) return;

                    if (userItems != null && !userItems.isEmpty()) {
                        showUserItems(userItems);
                        activateBuffs(userItems);
                    } else {
                        System.out.println("No items found for user: " + userId);
                    }

                    updateUI();
                    Toast.makeText(requireContext(), "All item buffs applied!", Toast.LENGTH_SHORT).show();
                });
    }
    private void activateBuffs(List<UserItemWithEquipmentDto> userItems) {
        for(UserItemWithEquipmentDto equipment:userItems){
            if(equipment.getAttribute().equals("money")){
                this.goldReward += (int)equipment.getBonus() * this.goldReward;
            }
            else if(equipment.getAttribute().equals("power")){
                this.pp += (int)(this.pp * equipment.getBonus());
            }
            else if(equipment.getAttribute().equals("attackChance")){
                double newHitChance = this.hitChance + equipment.getBonus() * this.hitChance;
                this.hitChance = Math.min(newHitChance, 1.0);
            }
        }
    }

    private void showUserItems(List<UserItemWithEquipmentDto> userItems) {
        LinearLayout container = requireView().findViewById(R.id.equipmentContainer);
        container.removeAllViews();

        UserItemArrayAdapter adapter = new UserItemArrayAdapter(requireContext(), userItems);

        for (int i = 0; i < adapter.getCount(); i++) {
            View itemView = adapter.getView(i, null, container);
            container.addView(itemView);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bgMusicPlayer = MediaPlayer.create(requireContext(), R.raw.boss1_music);
        bgMusicPlayer.setLooping(true);

        bgMusicPlayer.setOnPreparedListener(mp -> {
            mp.seekTo(10000);
            mp.start();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_boss_fight, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupSoundEffects();
        navController = NavHostFragment.findNavController(this);

        bossVideo = view.findViewById(R.id.bossVideo);
        tvHpValue = view.findViewById(R.id.tvHpValue);
        tvPpValue = view.findViewById(R.id.tvPpValue);
        tvAttackCountValue = view.findViewById(R.id.tvAttackCountValue);
        tvHitChanceValue = view.findViewById(R.id.tvHitChanceValue);
        btnBoss = view.findViewById(R.id.btnBoss);

        bossViewmodel = new ViewModelProvider(this).get(BossFightViewmodel.class);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            loadCurrentUser(firebaseUser.getUid());
            loadActiveBoss(firebaseUser.getUid());
            bossViewmodel.seedUserItems(firebaseUser.getUid()); // izbrisati kad bude postojali itemi
        } else {
            navController.popBackStack(R.id.mainMenuFragment, false);
        }

        Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.boss1_idle_anim);
        bossVideo.setVideoURI(uri);

        bossVideo.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            bossVideo.start();

            Sounds[] entrySounds = {Sounds.NOT_WELCOME, Sounds.BACKOFF};
            int index = (int) (Math.random() * entrySounds.length);
            playSound(entrySounds[index], 300);
        });

        btnBoss.setOnClickListener(v -> performAttack());

        shakeEnabled = true;

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeListener = createShakeListener();
    }

    private void playBossReaction(boolean hit) {
        btnBoss.setEnabled(false);
        shakeEnabled = false;
        bgMusicPlayer.setVolume(0.7f, 0.7f);

        if (hit) {
            int videoRes = R.raw.boss1_hit_anim;
            Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + videoRes);
            bossVideo.setVideoURI(uri);

            bossVideo.setOnPreparedListener(mp -> {
                mp.setLooping(false);
                bossVideo.start();
            });

            bossVideo.setOnCompletionListener(mp -> {
                Uri idleUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.boss1_idle_anim);
                bossVideo.setVideoURI(idleUri);

                bossVideo.setOnPreparedListener(idleMp -> {
                    idleMp.setLooping(true);
                    bossVideo.start();
                    btnBoss.setEnabled(true);
                    shakeEnabled = true;
                });
            });

            playSound(Sounds.SWORD, 2000);

            if (boss.getCurrentHp() <= 0) {
                playSound(Sounds.DEATH, 3200);
            } else {
                playSound(Sounds.GRUNT, 3200);
            }

        } else {
            int videoRes = R.raw.boss1_miss_anim;
            Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + videoRes);
            bossVideo.setVideoURI(uri);

            bossVideo.setOnPreparedListener(mp -> {
                mp.setLooping(false);
                bossVideo.start();
            });

            bossVideo.setOnCompletionListener(mp -> {
                Uri idleUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.boss1_idle_anim);
                bossVideo.setVideoURI(idleUri);

                bossVideo.setOnPreparedListener(idleMp -> {
                    idleMp.setLooping(true);
                    bossVideo.start();
                    btnBoss.setEnabled(true);
                    shakeEnabled = true;
                });
            });

            playSound(Sounds.PATHETHIC, 500);
            playSound(Sounds.LAUGH, 2000);
            playSound(Sounds.SMASH, 3100);
        }
        bgMusicPlayer.setVolume(1.0f, 1.0f);
    }

    private void updateUI() {
        tvHpValue.setText(String.valueOf(boss.getCurrentHp()));
        tvPpValue.setText(String.valueOf(pp));
        tvAttackCountValue.setText(String.valueOf(boss.getAttacksRemaining()));
        tvHitChanceValue.setText(String.valueOf((int)(this.hitChance * 100)));
    }

    private void performAttack() {
        if (boss.getAttacksRemaining() <= 0){
            btnBoss.setEnabled(false);
            return;
        }

        boss.setAttacksRemaining(boss.getAttacksRemaining() - 1);
        boolean hit = Math.random() < this.hitChance;

        if (hit) boss.setCurrentHp(boss.getCurrentHp() - pp);

        if(boss.getCurrentHp() < 0)
            boss.setCurrentHp(0);

        updateUI();
        playBossReaction(hit);

        Bundle args = new Bundle();
        boolean navigate;

        if (boss.getCurrentHp() == 0) {
            args.putBoolean("bossDefeated", true);
            args.putInt("gold", this.goldReward);

            Toast.makeText(requireContext(), "You got lucky!", Toast.LENGTH_SHORT).show();
            boss.setStatus(BossStatus.DEFEATED);
            navigate = true;
        } else if (boss.getAttacksRemaining() == 0 && boss.getCurrentHp() <= boss.getOriginalHp() / 2.0) {
            args.putBoolean("bossDefeated", false);
            args.putInt("gold", this.goldReward);

            Toast.makeText(requireContext(), "You got away this time!", Toast.LENGTH_SHORT).show();
            boss.setStatus(BossStatus.FAILED);
            navigate = true;
        } else if (boss.getAttacksRemaining() == 0 && boss.getCurrentHp() > boss.getOriginalHp() / 2.0) {
            playSound(Sounds.LAUGH, 1000);
            Toast.makeText(requireContext(), "You failed miserably!", Toast.LENGTH_SHORT).show();

            boss.setStatus(BossStatus.FAILED);
            navigate = true;
        } else{
            navigate = false;
        }

        // Save boss state after all logic
        bossViewmodel.saveBossLiveData(boss).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == Result.Status.SUCCESS) {
                Log.d("BossFight", "Boss state saved successfully");

                if (navigate) {
                    bossVideo.postDelayed(() -> {
                        if (args.isEmpty()) {
                            navController.navigate(R.id.action_FightFragment_to_mainMenuFragment);
                        } else {
                            navController.navigate(R.id.action_bossFightFragment_to_bossRewardFragment, args);
                        }
                    }, 4000);
                }
            } else {
                Log.e("BossFight", "Failed to save boss: " + result.getMessage());
            }
        });
    }

    private void playSound(Sounds sound, long delayMillis) {
        new Thread(() -> {
            try {
                if (delayMillis > 0) Thread.sleep(delayMillis);

                int soundToPlay;

                switch (sound) {
                    case BACKOFF:
                        soundToPlay = backoffSound;
                        break;
                    case DEATH:
                        soundToPlay = deathSound;
                        break;
                    case NOT_WELCOME:
                        soundToPlay = notWelcomeSound;
                        break;
                    case SMASH:
                        soundToPlay = smashSound;
                        break;
                    case SWORD:
                        soundToPlay = swordSound;
                        break;
                    case GRUNT:
                        int[] grunts = {grunt1Sound, grunt2Sound, grunt3Sound};
                        int index = (int) (Math.random() * grunts.length);
                        soundToPlay = grunts[index];
                        break;
                    case LAUGH:
                        soundToPlay = laughSound;
                        break;
                    case PATHETHIC:
                        soundToPlay = pathethicSound;
                        break;
                    default:
                        soundToPlay = laughSound;
                        break;
                }

                requireActivity().runOnUiThread(() ->
                        soundPool.play(soundToPlay, 0.9f, 0.9f, 1, 0, 1f));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void setupSoundEffects() {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .build();

        backoffSound = soundPool.load(requireContext(), R.raw.boss1_backoff_snd, 1);
        deathSound = soundPool.load(requireContext(), R.raw.boss1_death_snd, 1);
        notWelcomeSound = soundPool.load(requireContext(), R.raw.boss1_notwelcome_snd, 1);
        smashSound = soundPool.load(requireContext(), R.raw.boss1_smash_snd, 1);
        swordSound = soundPool.load(requireContext(), R.raw.sword_attack, 1);
        laughSound = soundPool.load(requireContext(), R.raw.boss1_laugh_snd, 1);
        grunt1Sound = soundPool.load(requireContext(), R.raw.boss1_grunt1_snd, 1);
        grunt2Sound = soundPool.load(requireContext(), R.raw.boss1_grunt2_snd, 1);
        grunt3Sound = soundPool.load(requireContext(), R.raw.boss1_grunt3_snd, 1);
        pathethicSound = soundPool.load(requireContext(), R.raw.boss1_pathetic_snd, 1);
    }

    private SensorEventListener createShakeListener() {
        return new SensorEventListener() {
            private static final float SHAKE_THRESHOLD = 2.5f; // much more realistic
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
                    if (currentTime - lastShakeTime > 800) { // avoid spam
                        lastShakeTime = currentTime;

                        if (shakeEnabled) {
                            performAttack(); // attack on shake
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bgMusicPlayer != null && bgMusicPlayer.isPlaying()) {
            bgMusicPlayer.pause();
        }

        sensorManager.unregisterListener(shakeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bgMusicPlayer != null) {
            bgMusicPlayer.start();
        }

        if (shakeEnabled) {
            sensorManager.registerListener(shakeListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);
        }

        // mislim da ipak nema potrebe za ovim
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser != null) {
//            loadCurrentUser(firebaseUser.getUid());
//            loadActiveBoss(firebaseUser.getUid());
//            loadUserItems(firebaseUser.getUid());
//        } else {
//            navController.popBackStack(R.id.mainMenuFragment, false);
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (bgMusicPlayer != null) {
            bgMusicPlayer.setVolume(1.0f, 1.0f);
            bgMusicPlayer.start();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (bgMusicPlayer != null) {
            bgMusicPlayer.release();
            bgMusicPlayer = null;
        }
    }
}