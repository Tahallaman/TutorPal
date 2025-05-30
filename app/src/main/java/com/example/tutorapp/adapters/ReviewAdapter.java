package com.example.tutorapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.R;
import com.example.tutorapp.models.ReviewModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    
    private Context context;
    private List<ReviewModel> reviews;
    private SimpleDateFormat dateFormat;
    
    public ReviewAdapter(Context context, List<ReviewModel> reviews) {
        this.context = context;
        this.reviews = reviews;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel review = reviews.get(position);
        
        // Set student name
        holder.studentName.setText(review.getStudentName());
        
        // Set rating
        holder.ratingBar.setRating((float) review.getRating());
        holder.ratingText.setText(String.valueOf(review.getRating()));
          // Set review text
        holder.reviewText.setText(review.getComment());
        
        // Set date
        if (review.getCreatedAt() != null) {
            holder.dateText.setText(dateFormat.format(review.getCreatedAt()));
        }
        
        // TODO: Load student profile image using Glide
        holder.profileImage.setImageResource(R.drawable.ic_tutor_placeholder);
        
        // Show/hide helpful count
        if (review.getHelpfulCount() > 0) {
            holder.helpfulText.setVisibility(View.VISIBLE);
            holder.helpfulText.setText(context.getString(R.string.helpful_count, review.getHelpfulCount()));
        } else {
            holder.helpfulText.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }
    
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView studentName, ratingText, reviewText, dateText, helpfulText;
        RatingBar ratingBar;
        
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            studentName = itemView.findViewById(R.id.studentName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ratingText = itemView.findViewById(R.id.ratingText);
            reviewText = itemView.findViewById(R.id.reviewText);
            dateText = itemView.findViewById(R.id.dateText);
            helpfulText = itemView.findViewById(R.id.helpfulText);
        }
    }
}
