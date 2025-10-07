package com.example.valorquest.ui.social;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.valorquest.R;
import com.example.valorquest.model.dto.AllianceMessageDto;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AllianceChatAdapter extends BaseAdapter {
    private Context context;
    private List<AllianceMessageDto> messages;
    private LayoutInflater inflater;

    public AllianceChatAdapter(Context context, List<AllianceMessageDto> messages) {
        this.context = context;
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
    }

    public void updateMessages(List<AllianceMessageDto> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages != null ? messages.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2; // Left and right message types
    }

    @Override
    public int getItemViewType(int position) {
        AllianceMessageDto message = messages.get(position);
        return message.isCurrentUser() ? 1 : 0; // 1 = right, 0 = left
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AllianceMessageDto message = messages.get(position);
        ViewHolder holder;

        if (convertView == null) {
            int layoutRes = message.isCurrentUser() ? R.layout.item_message_right : R.layout.item_message_left;
            convertView = inflater.inflate(layoutRes, parent, false);
            holder = new ViewHolder();
            holder.avatar = convertView.findViewById(R.id.message_avatar);
            holder.username = convertView.findViewById(R.id.message_username);
            holder.text = convertView.findViewById(R.id.message_text);
            holder.time = convertView.findViewById(R.id.message_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set avatar
        String avatarResourceName = "avatar_" + message.getSenderAvatarId();
        int avatarResId = context.getResources().getIdentifier(avatarResourceName, "drawable", context.getPackageName());
        if (avatarResId != 0) {
            holder.avatar.setImageResource(avatarResId);
        } else {
            holder.avatar.setImageResource(R.drawable.avatar_1); // Default avatar
        }

        // Set username
        holder.username.setText(message.getSenderUsername());

        // Set message text
        holder.text.setText(message.getText());

        // Set timestamp
        String timeString = formatTimestamp(message.getTimestamp());
        holder.time.setText(timeString);

        return convertView;
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    private static class ViewHolder {
        ImageView avatar;
        TextView username;
        TextView text;
        TextView time;
    }
}
