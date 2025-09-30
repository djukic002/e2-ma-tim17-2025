package com.example.valorquest.ui.social;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.valorquest.R;
import com.example.valorquest.model.User;

import java.util.List;

public class SocialUserAdapter extends ArrayAdapter<User> {
    public enum Mode {
        FRIEND_LIST,
        ADD_FRIEND,
        ALLIANCE_SELECT
    }

    private final Context context;
    private final Mode mode;
    private final ActionCallback callback;

    public interface ActionCallback {
        void onViewProfile(User user);
        void onRemoveFriend(User user);
        void onAddFriend(User user);
        void onSelectForAlliance(User user);
    }

    public SocialUserAdapter(@NonNull Context context,
                             @NonNull List<User> users,
                             @NonNull Mode mode,
                             ActionCallback callback) {
        super(context, 0, users);
        this.context = context;
        this.mode = mode;
        this.callback = callback;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_friend_card, parent, false);
        }

        User user = getItem(position);

        ImageView avatar = convertView.findViewById(R.id.friend_avatar);
        TextView username = convertView.findViewById(R.id.friend_username);
        TextView email = convertView.findViewById(R.id.friend_email);
        Button btnProfile = convertView.findViewById(R.id.btn_view_profile);
        Button btnAction = convertView.findViewById(R.id.btn_action);

        if (user != null) {
            username.setText(user.getUsername());
            email.setText(user.getEmail());

            int avatarResId = context.getResources()
                    .getIdentifier("avatar_" + user.getAvatarId(),
                            "drawable", context.getPackageName());
            if (avatarResId != 0) {
                avatar.setImageResource(avatarResId);
            }

            btnProfile.setOnClickListener(v -> {
                if (callback != null) callback.onViewProfile(user);
                else Toast.makeText(context, "Profile of " + user.getUsername(),
                        Toast.LENGTH_SHORT).show();
            });

            switch (mode) {
                case FRIEND_LIST:
                    btnAction.setText("X");
                    btnAction.setOnClickListener(v -> {
                        if (callback != null) callback.onRemoveFriend(user);
                    });
                    break;

                case ADD_FRIEND:
                    btnAction.setText("+");
                    btnAction.setOnClickListener(v -> {
                        if (callback != null) callback.onAddFriend(user);
                    });
                    break;

                case ALLIANCE_SELECT:
                    btnAction.setText("Invite");
                    btnAction.setOnClickListener(v -> {
                        if (callback != null) callback.onSelectForAlliance(user);
                    });
                    break;
            }
        }

        return convertView;
    }
}
