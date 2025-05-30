package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tutorapp.adapters.ImageSliderAdapter;
import com.example.tutorapp.adapters.ReviewAdapter;
import com.example.tutorapp.models.ReviewModel;
import com.example.tutorapp.models.TutorModel;
import com.example.tutorapp.utils.AuthUtils;
import com.example.tutorapp.utils.FavouritesManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "DetailsActivity";
    
    private FirebaseFirestore db;
    private FavouritesManager favouritesManager;
    private String tutorId;
    private TutorModel currentTutor;
    
    // UI Components
    private ViewPager2 imageSlider;
    private ImageSliderAdapter imageSliderAdapter;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView tutorName, tutorSubject, tutorBio, tutorLocation, tutorPrice;
    private TextView experienceText, availabilityText;
    private RatingBar tutorRating;
    private TextView ratingCount;
    private RecyclerView reviewsRecyclerView;
    private Button bookNowButton, chatButton;
    private FloatingActionButton favoriteFab;
    
    private List<ReviewModel> reviewsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        
        // Initialize Firestore and FavouritesManager
        db = FirebaseFirestore.getInstance();
        favouritesManager = new FavouritesManager(this);
          // Get tutor ID from intent
        tutorId = getIntent().getStringExtra("tutor_id");
        Log.d(TAG, "Received tutor_id: " + tutorId);
        if (tutorId == null) {
            Toast.makeText(this, "Error: Tutor not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupToolbar();
        setupImageSlider();
        setupButtons();
        loadTutorDetails();
        loadReviews();
        updateFavoriteButton();
    }
    
    private void initializeViews() {
        imageSlider = findViewById(R.id.imageSlider);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        tutorName = findViewById(R.id.tutorName);
        tutorSubject = findViewById(R.id.tutorSubject);
        tutorBio = findViewById(R.id.tutorBio);
        tutorLocation = findViewById(R.id.tutorLocation);
        tutorPrice = findViewById(R.id.tutorPrice);        experienceText = findViewById(R.id.experienceText);
        availabilityText = findViewById(R.id.availabilityText);
        tutorRating = findViewById(R.id.tutorRating);
        ratingCount = findViewById(R.id.ratingCount);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        bookNowButton = findViewById(R.id.bookNowButton);
        chatButton = findViewById(R.id.chatButton);
        favoriteFab = findViewById(R.id.favoriteFab);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupImageSlider() {
        imageSliderAdapter = new ImageSliderAdapter(this, new ArrayList<>());
        imageSlider.setAdapter(imageSliderAdapter);
    }
      private void setupButtons() {
        bookNowButton.setOnClickListener(v -> {
            // Check authentication before allowing booking
            if (!AuthUtils.requireAuthentication(this, "book a tutoring session")) {
                return;
            }
            
            if (currentTutor != null) {
                Intent intent = new Intent(DetailsActivity.this, BookingActivity.class);
                intent.putExtra("tutor_id", tutorId);
                intent.putExtra("tutor_name", currentTutor.getName());
                intent.putExtra("tutor_subject", currentTutor.getSubject());
                intent.putExtra("tutor_price", currentTutor.getPricePerHour());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please wait for tutor details to load", Toast.LENGTH_SHORT).show();
            }
        });

        chatButton.setOnClickListener(v -> {
            // Check authentication before allowing messaging
            if (!AuthUtils.requireAuthentication(this, "send messages")) {
                return;
            }
            
            if (currentTutor != null) {
                Intent intent = new Intent(DetailsActivity.this, ChatActivity.class);
                intent.putExtra("tutor_id", tutorId);
                intent.putExtra("tutor_name", currentTutor.getName());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please wait for tutor details to load", Toast.LENGTH_SHORT).show();
            }
        });
        
        favoriteFab.setOnClickListener(v -> toggleFavorite());
    }
    
    private void toggleFavorite() {
        if (currentTutor == null) {
            Toast.makeText(this, "Please wait for tutor details to load", Toast.LENGTH_SHORT).show();
            return;
        }
        
        favouritesManager.toggleFavorite(tutorId, new FavouritesManager.FavouritesCallback() {
            @Override
            public void onSuccess() {
                boolean isFavorite = favouritesManager.isFavorite(tutorId);
                String message = isFavorite 
                    ? "Added " + currentTutor.getName() + " to favorites"
                    : "Removed " + currentTutor.getName() + " from favorites";
                Toast.makeText(DetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                updateFavoriteButton();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(DetailsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateFavoriteButton() {
        boolean isFavorite = favouritesManager.isFavorite(tutorId);
        if (isFavorite) {
            favoriteFab.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            favoriteFab.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }
      private void loadTutorDetails() {
        Log.d(TAG, "Loading tutor details for ID: " + tutorId);
        db.collection("tutors")
                .document(tutorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Document lookup completed. Exists: " + documentSnapshot.exists());
                    if (documentSnapshot.exists()) {
                        currentTutor = documentSnapshot.toObject(TutorModel.class);
                        if (currentTutor != null) {
                            Log.d(TAG, "Successfully loaded tutor: " + currentTutor.getName());
                            updateUI();
                        } else {
                            Log.e(TAG, "Failed to convert document to TutorModel");
                            Toast.makeText(this, "Error parsing tutor data", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Document does not exist for tutor ID: " + tutorId);
                        Toast.makeText(this, "Tutor not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading tutor details", e);
                    Toast.makeText(this, "Error loading tutor details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
    
    private void updateUI() {
        if (currentTutor == null) return;
        
        // Set basic info
        tutorName.setText(currentTutor.getName());
        tutorSubject.setText(currentTutor.getSubject());
        tutorBio.setText(currentTutor.getBio());        tutorLocation.setText(currentTutor.getLocation());
        tutorPrice.setText(String.format("$%.0f/hour", currentTutor.getPricePerHour()));
          // Set rating
        tutorRating.setRating((float) currentTutor.getRatingAverage());
        ratingCount.setText(String.format("(%d reviews)", currentTutor.getReviewCount()));
          // Set additional details        experienceText.setText(String.format("%s of experience", currentTutor.getExperience()));
        
        if (currentTutor.getAvailability() != null && !currentTutor.getAvailability().isEmpty()) {
            // Convert Map to readable string
            StringBuilder availabilityBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : currentTutor.getAvailability().entrySet()) {
                if (availabilityBuilder.length() > 0) {
                    availabilityBuilder.append(", ");
                }
                availabilityBuilder.append(entry.getKey()).append(": ").append(entry.getValue());
            }
            availabilityText.setText(availabilityBuilder.toString());
        }
        
        // Update toolbar title
        collapsingToolbar.setTitle(currentTutor.getName());
        
        // Update image slider
        if (currentTutor.getImageUrls() != null && !currentTutor.getImageUrls().isEmpty()) {
            imageSliderAdapter.updateImages(currentTutor.getImageUrls());
        } else {
            // Add placeholder image if no images available
            List<String> placeholderImages = new ArrayList<>();
            placeholderImages.add(""); // Empty string for placeholder
            imageSliderAdapter.updateImages(placeholderImages);
        }
    }    private void loadReviews() {
        db.collection("reviews")
                .whereEqualTo("tutorID", tutorId)  // Use correct field name to match ReviewModel
                .limit(20)  // Increased limit since we'll sort client-side
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviewsList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ReviewModel review = document.toObject(ReviewModel.class);
                        if (review != null) {
                            reviewsList.add(review);
                        }
                    }
                    
                    // Sort reviews by creation date (most recent first) - client-side sorting
                    reviewsList.sort((r1, r2) -> {
                        if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                        if (r1.getCreatedAt() == null) return 1;
                        if (r2.getCreatedAt() == null) return -1;
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt()); // Descending order
                    });
                    
                    // Limit to top 10 reviews after sorting
                    if (reviewsList.size() > 10) {
                        reviewsList = reviewsList.subList(0, 10);
                    }
                    
                    setupReviewsRecyclerView();
                    Log.d(TAG, "Loaded " + reviewsList.size() + " reviews for tutor: " + tutorId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reviews", e);
                    // Show user-friendly error message
                    Toast.makeText(this, "Could not load reviews. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
      private void setupReviewsRecyclerView() {
        ReviewAdapter reviewAdapter = new ReviewAdapter(this, reviewsList);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }
}
