package com.example.tutorapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.R;
import com.example.tutorapp.models.TutorModel;
import com.example.tutorapp.utils.FavouritesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TutorListAdapter extends RecyclerView.Adapter<TutorListAdapter.TutorViewHolder> {
      private List<TutorModel> tutors;
    private OnTutorClickListener onTutorClickListener;
    private OnFavoriteClickListener onFavoriteClickListener;
    private DecimalFormat decimalFormat;
    private FavouritesManager favouritesManager;
    
    public interface OnTutorClickListener {
        void onTutorClick(TutorModel tutor);
    }
    
    public interface OnFavoriteClickListener {
        void onFavoriteClick(TutorModel tutor);
    }
      public TutorListAdapter(List<TutorModel> tutors, 
                           OnTutorClickListener onTutorClickListener,
                           OnFavoriteClickListener onFavoriteClickListener) {
        this.tutors = tutors;
        this.onTutorClickListener = onTutorClickListener;
        this.onFavoriteClickListener = onFavoriteClickListener;
        this.decimalFormat = new DecimalFormat("#.#");
    }
    
    public void setFavouritesManager(FavouritesManager favouritesManager) {
        this.favouritesManager = favouritesManager;
    }
    
    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutor, parent, false);
        return new TutorViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int position) {
        TutorModel tutor = tutors.get(position);
        holder.bind(tutor);
    }
    
    @Override
    public int getItemCount() {
        return tutors.size();
    }
    
    class TutorViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private CircleImageView profileImage;
        private TextView tutorName;
        private TextView ratingText;
        private TextView subjectText;
        private TextView locationText;
        private TextView experienceText;
        private TextView priceText;
        private MaterialButton favoriteButton;
        private MaterialButton viewProfileButton;
        
        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            profileImage = itemView.findViewById(R.id.tutor_profile_image);
            tutorName = itemView.findViewById(R.id.tutor_name);
            ratingText = itemView.findViewById(R.id.rating_text);
            subjectText = itemView.findViewById(R.id.subject_text);
            locationText = itemView.findViewById(R.id.location_text);
            experienceText = itemView.findViewById(R.id.experience_text);
            priceText = itemView.findViewById(R.id.price_text);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            viewProfileButton = itemView.findViewById(R.id.view_profile_button);
        }
        
        public void bind(TutorModel tutor) {
            tutorName.setText(tutor.getName());
            subjectText.setText(tutor.getSubject());
            locationText.setText("📍 " + tutor.getLocation());
            experienceText.setText(tutor.getExperience());
            
            // Format rating
            if (tutor.getRatingAverage() > 0) {
                ratingText.setText(decimalFormat.format(tutor.getRatingAverage()));
            } else {
                ratingText.setText("0.0");
            }
            
            // Format price
            priceText.setText("$" + (int) tutor.getHourlyRate() + "/hour");
            
            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (onTutorClickListener != null) {
                    onTutorClickListener.onTutorClick(tutor);
                }
            });
            
            viewProfileButton.setOnClickListener(v -> {
                if (onTutorClickListener != null) {
                    onTutorClickListener.onTutorClick(tutor);
                }
            });
              favoriteButton.setOnClickListener(v -> {
                if (onFavoriteClickListener != null) {
                    onFavoriteClickListener.onFavoriteClick(tutor);
                }
            });
            
            // Update favorite button appearance based on favorite status
            if (favouritesManager != null && favouritesManager.isFavorite(tutor.getTutorId())) {
                favoriteButton.setIcon(favoriteButton.getContext().getDrawable(android.R.drawable.btn_star_big_on));
                favoriteButton.setIconTint(favoriteButton.getContext().getColorStateList(com.google.android.material.R.color.design_default_color_secondary));
            } else {
                favoriteButton.setIcon(favoriteButton.getContext().getDrawable(android.R.drawable.btn_star_big_off));
                favoriteButton.setIconTint(favoriteButton.getContext().getColorStateList(com.google.android.material.R.color.material_on_surface_disabled));
            }
            
            // TODO: Load profile image using image loading library like Glide or Picasso
            // For now, using default image
        }
    }
    
    public void updateTutors(List<TutorModel> newTutors) {
        this.tutors = newTutors;
        notifyDataSetChanged();
    }
}
