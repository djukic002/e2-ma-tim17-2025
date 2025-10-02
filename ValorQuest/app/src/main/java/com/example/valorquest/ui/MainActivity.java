package com.example.valorquest.ui;

import android.content.Intent;
import android.os.Bundle;

import com.example.valorquest.R;
import com.example.valorquest.data.repositories.AllianceNotificationRepository;
import com.example.valorquest.data.repositories.AllianceRepository;
import com.example.valorquest.data.repositories.UserRepository;
import com.example.valorquest.service.AllianceService;
import com.example.valorquest.service.FriendService;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.valorquest.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(view -> {
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() != R.id.mainMenuFragment) {
                navController.navigate(R.id.mainMenuFragment);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SplashActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("CAO IZ MAIN ACTIVITIY", "");
        super.onNewIntent(intent);
        handleAllianceInviteIntent(intent);
    }

    private void handleAllianceInviteIntent(Intent intent) {
        if (intent != null && "ACTION_ACCEPT_INVITE".equals(intent.getAction())) {
            String allianceId = intent.getStringExtra("allianceId");
            String senderId = intent.getStringExtra("senderId");
            String notificationId = intent.getStringExtra("notificationId");

            AllianceService allianceService = new AllianceService(new AllianceRepository(), new AllianceNotificationRepository(), new UserRepository(), new FriendService(new UserRepository()));
            allianceService.isCurrentUserInAlliance(isInAlliance -> {
                if (isInAlliance)
                    showAllianceDecisionDialog(allianceId, senderId, notificationId, allianceService);
                else {
                    allianceService.acceptInvite(allianceId, notificationId, senderId);
                    NotificationManagerCompat.from(this).cancel(notificationId.hashCode());
                    Toast.makeText(this, "Welcome to the alliance!", Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

    private void showAllianceDecisionDialog(String allianceId, String senderId, String notificationId, AllianceService allianceService) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        allianceService.isUserLeader(FirebaseAuth.getInstance().getCurrentUser().getUid(), isLeader -> {
            if (isLeader) {
                builder.setTitle("Disband Current Alliance?")
                        .setMessage("You are the leader of your current alliance. To join the new one, you must disband it. Continue?")
                        .setPositiveButton("Disband & Join", (dialog, which) -> {
//                            disbandAlliance(currentAllianceId);
                            allianceService.acceptInvite(allianceId, notificationId, senderId);
                            NotificationManagerCompat.from(this).cancel(notificationId.hashCode());
                            Toast.makeText(this, "Welcome to the new alliance!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {});
            } else {
                builder.setTitle("Leave Current Alliance?")
                        .setMessage("You are already in an alliance. Do you want to leave it and join the new one?")
                        .setPositiveButton("Leave & Join", (dialog, which) -> {
//                            leaveAlliance(currentAllianceId);
                            allianceService.acceptInvite(allianceId, notificationId, senderId);
                            NotificationManagerCompat.from(this).cancel(notificationId.hashCode());
                            Toast.makeText(this, "Welcome to the new alliance!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {});
            }
            builder.setCancelable(false);
            builder.show();
        });
    }
}