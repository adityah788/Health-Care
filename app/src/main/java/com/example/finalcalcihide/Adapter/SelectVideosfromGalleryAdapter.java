package com.example.finalcalcihide.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import com.bumptech.glide.request.RequestOptions;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.ViewModel.VideoModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SelectVideosfromGalleryAdapter extends RecyclerView.Adapter<SelectVideosfromGalleryAdapter.ViewHolder> {



    private final Context context;
    private final ArrayList<String> imagePaths;
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> hashSetselectedItems = new HashSet<>();
    private List<VideoModel> videos;



    public SelectVideosfromGalleryAdapter(Context context, ArrayList<String> imagePaths, OnItemSelectedListener listener, List<VideoModel> videos) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.listener = listener;
        this.videos = videos;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);

        void onSelectionChanged(boolean isSelected);
    }

    @NonNull
    @Override
    public SelectVideosfromGalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new SelectVideosfromGalleryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        VideoModel video = videos.get(position);

        // Load thumbnail using Picasso
        Picasso.get()
                .load(video.getUri())
                .resize(300, 300) // Resize to make it a thumbnail
                .centerCrop() // Crop to maintain aspect ratio
                .placeholder(R.drawable.reel) // Placeholder while loading
                .error(R.drawable.baseline_cancel_24) // Error image if loading fails
                .into(holder.imageView);



        boolean isSelected = hashSetselectedItems.contains(position);
        holder.imageView.setColorFilter(isSelected ? ContextCompat.getColor(context, R.color.overlayColor) : Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
        holder.imageViewTick.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.imageViewUnTick.setVisibility(isSelected ? View.GONE : View.VISIBLE);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemSelected(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(position);
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return videos.size();

    }

    private boolean isVideoFile(File file) {
        String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov"};
        for (String extension : videoExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getVideoDuration(File file) {
        // Placeholder method to get video duration
        // Replace this with actual implementation to get video duration
        return "00:00"; // Default placeholder duration
    }

    public void toggleSelection(int position) {
        if (hashSetselectedItems.contains(position)) {
            hashSetselectedItems.remove(position);
        } else {
            hashSetselectedItems.add(position);
        }
        notifyItemChanged(position);
        listener.onSelectionChanged(!hashSetselectedItems.isEmpty());
    }

    public void clearSelection() {
        hashSetselectedItems.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(false);
    }

    public void selectAll(boolean selectAll) {
        hashSetselectedItems.clear();
        if (selectAll) {
            for (int i = 0; i < getItemCount(); i++) {
                hashSetselectedItems.add(i);
            }
        }
        notifyDataSetChanged();
        listener.onSelectionChanged(!hashSetselectedItems.isEmpty());
    }

    public int getSelectedItemCount() {
        return hashSetselectedItems.size();
    }

    public boolean isSelectedAny() {
        return !hashSetselectedItems.isEmpty();
    }


    // In your SelectImageVideosAdapter
    public HashSet<Integer> getSelectedItems() {
        return hashSetselectedItems;
    }


    public List<String> getSelectedImagePaths() {
        List<String> selectedPaths = new ArrayList<>();
        for (int position : hashSetselectedItems) {
            selectedPaths.add(imagePaths.get(position));
        }
        return selectedPaths;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView imageViewTick,imageViewUnTick;
        TextView videoDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item_Imageview);
            imageViewTick = itemView.findViewById(R.id.tickMarkImageView);
            imageViewUnTick = itemView.findViewById(R.id.untickMarkImageView);
            videoDuration = itemView.findViewById(R.id.image_item_duration);
        }
    }

}