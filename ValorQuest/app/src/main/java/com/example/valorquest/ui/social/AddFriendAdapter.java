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

public class AddFriendAdapter extends ArrayAdapter<User> {
    private final Context context;
    private final FriendActionCallback callback;

    // Callback interface for "add friend" button
    public interface FriendActionCallback {
        void onAddFriend(User user);
    }

    public AddFriendAdapter(@NonNull Context context, @NonNull List<User> users, FriendActionCallback callback) {
        super(context, 0, users);
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friend_card, parent, false);
        }

        User user = getItem(position);

        ImageView avatar = convertView.findViewById(R.id.friend_avatar);
        TextView username = convertView.findViewById(R.id.friend_username);
        Button btnProfile = convertView.findViewById(R.id.btn_view_profile);
        Button btnAdd = convertView.findViewById(R.id.btn_action);

        if (user != null) {
            username.setText(user.getUsername());

            // Load avatar dynamically
            int avatarResId = context.getResources()
                    .getIdentifier("avatar_" + user.getAvatarId(), "drawable", context.getPackageName());
            if (avatarResId != 0) {
                avatar.setImageResource(avatarResId);
            }

            btnProfile.setOnClickListener(v ->
                    Toast.makeText(context, "Profile of " + user.getUsername(), Toast.LENGTH_SHORT).show()
            );

            // Add friend button
            btnAdd.setText("+");
            btnAdd.setOnClickListener(v -> {
                if (callback != null) callback.onAddFriend(user);
            });
        }

        return convertView;
    }
}
