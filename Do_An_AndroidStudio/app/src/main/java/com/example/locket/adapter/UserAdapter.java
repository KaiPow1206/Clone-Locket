package com.example.locket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.data.model.Users.*;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<SearchUserResponse> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(SearchUserResponse user);
    }

    public UserAdapter(List<SearchUserResponse> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void setUserList(List<SearchUserResponse> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        SearchUserResponse user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView textUsername;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            textUsername = itemView.findViewById(R.id.textUsername);
        }

        void bind(final SearchUserResponse user) {
            textUsername.setText(user.getUsername());
            if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getAvatar_url())
                        .placeholder(R.drawable.default_avatar)
                        .into(imgAvatar);

            } else {
                imgAvatar.setImageResource(R.drawable.default_avatar);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }
    }
}
