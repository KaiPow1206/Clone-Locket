package com.example.locket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.data.model.Notifications.*;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Lấy username an toàn (null-safe)
        String senderName = "Người dùng";
        if (notification.getSender() != null && notification.getSender().getUsername() != null) {
            senderName = notification.getSender().getUsername();
        }

        String message;
        switch (notification.getType()) {
            case "friend_request":
                message = senderName + " muốn kết bạn với bạn.";
                break;
            case "friend_accepted":
                message = senderName + " đã đồng ý kết bạn.";
                break;
            case "reaction":
                message = senderName + " đã tương tác với ảnh của bạn.";
                break;
            default:
                message = "Thông báo không xác định.";
        }

        holder.textMessage.setText(message);
    }

    public void clearNotifications() {
        if (notificationList != null) {
            notificationList.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.tvNotificationMessage);
        }
    }
}
