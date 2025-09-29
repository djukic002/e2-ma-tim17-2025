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

public class FriendListAdapter extends ArrayAdapter<User> {
    private final Context context;
    public FriendListAdapter(@NonNull Context context, @NonNull List<User> friends) {
        super(context, 0, friends);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friend_card, parent, false);
        }

        User friend = getItem(position);

        ImageView avatar = convertView.findViewById(R.id.friend_avatar);
        TextView username = convertView.findViewById(R.id.friend_username);
        TextView email = convertView.findViewById(R.id.friend_email);
        Button btnProfile = convertView.findViewById(R.id.btn_view_profile);

        if (friend != null) {
            username.setText(friend.getUsername());
            email.setText(friend.getEmail());

            // Avatar by avatarId (example)
            int avatarResId = getContext().getResources()
                    .getIdentifier("avatar_" + friend.getAvatarId(), "drawable", getContext().getPackageName());
            if (avatarResId != 0) {
                avatar.setImageResource(avatarResId);
            }

            btnProfile.setOnClickListener(v -> {
                // TODO: Navigate to profile fragment
                Toast.makeText(getContext(), "Profile of " + friend.getUsername(), Toast.LENGTH_SHORT).show();
            });
        }

        return convertView;
    }
}
