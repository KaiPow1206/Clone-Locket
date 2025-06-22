package com.example.locket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.locket.R;
import com.example.locket.data.model.Photos.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private List<Photo> photoList;
    private OnPhotoClickListener listener;
    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_PHOTO = 1;

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    public PhotoAdapter(List<Photo> photoList, OnPhotoClickListener listener) {
        this.photoList = photoList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_EMPTY;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_no_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.textEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        TextView textEmpty;
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            textEmpty = itemView.findViewById(R.id.textEmpty);
        }
    }
}