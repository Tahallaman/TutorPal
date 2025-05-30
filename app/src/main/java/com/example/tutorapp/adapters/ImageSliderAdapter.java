package com.example.tutorapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.R;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {
    
    private Context context;
    private List<String> imageUrls;
    
    public ImageSliderAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_slider_image, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // TODO: Use Glide or Picasso to load image from URL
            // For now, set a placeholder
            holder.imageView.setImageResource(R.drawable.ic_tutor_placeholder);
        } else {
            // Set placeholder image
            holder.imageView.setImageResource(R.drawable.ic_tutor_placeholder);
        }
        
        // Set scale type for proper image display
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
    
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }
    
    public void updateImages(List<String> newImageUrls) {
        this.imageUrls = newImageUrls;
        notifyDataSetChanged();
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sliderImageView);
        }
    }
}
