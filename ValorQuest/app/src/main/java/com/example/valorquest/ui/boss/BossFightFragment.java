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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.valorquest.R;
import com.example.valorquest.model.Boss;
import com.example.valorquest.model.Result;
import com.example.valorquest.viewmodel.BossFightViewmodel;
import com.example.valorquest.viewmodel.QuestsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BossFightFragment extends Fragment {
    private int pp = 50;
    private BossFightViewmodel bossViewmodel;
    private Boss boss;
    private MediaPlayer bgMusicPlayer;
    private VideoView bossVideo;
    private TextView tvHpValue, tvPpValue, tvAttackCountValue, tvHitChanceValue;
    private MaterialButton btnBoss;
    private SoundPool soundPool;
    private int backoffSound, deathSound, notWelcomeSound, smashSound, swordSound, laughSound ,grunt1Sound, grunt2Sound, grunt3Sound, pathethicSound;
    private enum Sounds{BACKOFF, DEATH, NOT_WELCOME, SMASH, SWORD, GRUNT, LAUGH, PATHETHIC};

    // Add these fields in your fragment
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener shakeListener;
    private boolean shakeEnabled;

    public BossFightFragment() {
    }
    private void loadActiveBoss(String userId) {
        bossViewmodel.getActiveBossForUser(userId).observe(getViewLifecycleOwner(), result -> {
            if (result.getStatus() == Result.Status.SUCCESS) {
                boss = result.getData();
                updateUI();
            } else {
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        bossVideo = view.findViewById(R.id.bossVideo);

        Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.boss1_idle_anim);
        bossVideo.setVideoURI(uri);

        bossVideo.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            bossVideo.start();

            Sounds[] entrySounds = {Sounds.NOT_WELCOME, Sounds.BACKOFF};
            int index = (int) (Math.random() * entrySounds.length);
            playSound(entrySounds[index], 0);
        });

        tvHpValue = view.findViewById(R.id.tvHpValue);
        tvPpValue = view.findViewById(R.id.tvPpValue);
        tvAttackCountValue = view.findViewById(R.id.tvAttackCountValue);
        tvHitChanceValue = view.findViewById(R.id.tvHitChanceValue);
        btnBoss = view.findViewById(R.id.btnBoss);

        shakeEnabled = true;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        bossViewmodel = new ViewModelProvider(this).get(BossFightViewmodel.class);
        if(user != null)
            loadActiveBoss(user.getUid());

        // button attack
        btnBoss.setOnClickListener(v -> performAttack());

        // shake attack
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
                // Switch back to idle animation
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
        tvHitChanceValue.setText(String.valueOf((int)(boss.getHitChance() * 100)));
    }

    private void performAttack() {
        if (boss.getAttacksRemaining() <= 0){
            btnBoss.setEnabled(false);
            return;
        }

        boss.setAttacksRemaining(boss.getAttacksRemaining() - 1);
        boolean hit = Math.random() < boss.getHitChance();

        if (hit) boss.setCurrentHp(boss.getCurrentHp() - pp);

        if(boss.getCurrentHp() < 0)
            boss.setCurrentHp(0);

        updateUI();
        playBossReaction(hit);

        NavController navController = NavHostFragment.findNavController(this);

        if (boss.getCurrentHp() == 0) {
            bossVideo.postDelayed(() -> {
                Bundle args = new Bundle();
                args.putBoolean("bossDefeated", true);
                Toast.makeText(requireContext(), "You got lucky!", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_bossFightFragment_to_bossRewardFragment, args);
            }, 4000);
        }
        else if(boss.getAttacksRemaining() == 0 && boss.getCurrentHp() <= boss.getOriginalHp() / 2.0){
            bossVideo.postDelayed(() -> {
                Bundle args = new Bundle();
                args.putBoolean("bossDefeated", false);
                Toast.makeText(requireContext(), "You got away this time!", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_bossFightFragment_to_bossRewardFragment, args);
            }, 4000);
        }
        else if(boss.getAttacksRemaining() == 0 && boss.getCurrentHp() > boss.getOriginalHp() / 2.0){
            playSound(Sounds.LAUGH, 1000);
            bossVideo.postDelayed(() -> {
                Toast.makeText(requireContext(), "You failed miserably!", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_FightFragment_to_mainMenuFragment);
            }, 6000);
        }
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