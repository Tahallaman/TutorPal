package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.adapters.TutorListAdapter;
import com.example.tutorapp.models.TutorModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    
    private FirebaseFirestore db;
    
    // UI Components
    private EditText searchEditText;
    private Spinner subjectSpinner, locationSpinner;
    private SeekBar priceRangeSeekBar;
    private TextView priceRangeText;
    private RatingBar minRatingBar;
    private CheckBox onlineCheckBox, inPersonCheckBox;
    private ChipGroup experienceChipGroup;
    private Button searchButton, clearFiltersButton;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar progressBar;
    private TextView noResultsText;
    private LinearLayout filtersContainer;
    
    private TutorListAdapter searchResultsAdapter;
    private List<TutorModel> searchResults = new ArrayList<>();
    
    // Filter values
    private String searchQuery = "";
    private String selectedSubject = "";    private String selectedLocation = "";
    private double maxPrice = 100.0;
    private double minRating = 0.0;
    private boolean includeOnline = true;
    private boolean includeInPerson = true;
    private String selectedExperience = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupToolbar();
        setupFilters();
        setupRecyclerView();
        setupButtons();
        
        // Check if there's an initial search query from intent
        String initialQuery = getIntent().getStringExtra("search_query");
        if (!TextUtils.isEmpty(initialQuery)) {
            searchEditText.setText(initialQuery);
            performSearch();
        }
    }
    
    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        locationSpinner = findViewById(R.id.locationSpinner);
        priceRangeSeekBar = findViewById(R.id.priceRangeSeekBar);
        priceRangeText = findViewById(R.id.priceRangeText);        minRatingBar = findViewById(R.id.minRatingBar);
        onlineCheckBox = findViewById(R.id.onlineCheckBox);
        inPersonCheckBox = findViewById(R.id.inPersonCheckBox);
        experienceChipGroup = findViewById(R.id.experienceChipGroup);
        searchButton = findViewById(R.id.searchButton);
        clearFiltersButton = findViewById(R.id.clearFiltersButton);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        noResultsText = findViewById(R.id.noResultsText);
        filtersContainer = findViewById(R.id.filtersContainer);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Search Tutors");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupFilters() {
        // Setup price range slider
        priceRangeSeekBar.setMax(200);
        priceRangeSeekBar.setProgress(100);
        priceRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxPrice = progress;
                priceRangeText.setText(String.format("Up to $%d/hour", progress));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
          // Setup rating bar
        minRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            minRating = rating;
        });
        
        // Setup experience chips
        setupExperienceChips();
        
        // Set default values
        onlineCheckBox.setChecked(true);
        inPersonCheckBox.setChecked(true);
        priceRangeText.setText("Up to $100/hour");    }
    
    private void setupExperienceChips() {
        String[] experienceLevels = {"0-1 years", "2-5 years", "5-10 years", "10+ years"};
        
        for (String level : experienceLevels) {
            Chip chip = new Chip(this);
            chip.setText(level);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((view, isChecked) -> {
                if (isChecked) {
                    // Clear other experience selections (single choice)
                    for (int i = 0; i < experienceChipGroup.getChildCount(); i++) {
                        Chip otherChip = (Chip) experienceChipGroup.getChildAt(i);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                    selectedExperience = level;
                } else {
                    selectedExperience = "";
                }
            });
            experienceChipGroup.addView(chip);
        }
    }
      private void setupRecyclerView() {
        searchResultsAdapter = new TutorListAdapter(searchResults, tutor -> {
            // Handle tutor click - navigate to details
            Intent intent = new Intent(SearchActivity.this, DetailsActivity.class);
            intent.putExtra("tutor_id", tutor.getId());
            startActivity(intent);
        }, tutor -> {
            // Handle favorite click - toggle favorite status
            // TODO: Implement favorite functionality
        });
        
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
    }
    
    private void setupButtons() {
        searchButton.setOnClickListener(v -> performSearch());
        
        clearFiltersButton.setOnClickListener(v -> clearFilters());
    }
    
    private void clearFilters() {
        searchEditText.setText("");
        subjectSpinner.setSelection(0);
        locationSpinner.setSelection(0);
        priceRangeSeekBar.setProgress(100);
        minRatingBar.setRating(0);
        onlineCheckBox.setChecked(true);
        inPersonCheckBox.setChecked(true);
          // Clear experience chips
        for (int i = 0; i < experienceChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) experienceChipGroup.getChildAt(i);
            chip.setChecked(false);
        }
        
        selectedExperience = "";
        
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
    }
    
    private void performSearch() {
        // Get current filter values
        searchQuery = searchEditText.getText().toString().trim();
        selectedSubject = subjectSpinner.getSelectedItem() != null ? 
                         subjectSpinner.getSelectedItem().toString() : "";
        selectedLocation = locationSpinner.getSelectedItem() != null ? 
                          locationSpinner.getSelectedItem().toString() : "";
        includeOnline = onlineCheckBox.isChecked();
        includeInPerson = inPersonCheckBox.isChecked();
        
        showLoading(true);
        
        // Build Firestore query
        Query query = db.collection("tutors");
        
        // Apply text search filter (if supported by your Firestore setup)
        if (!TextUtils.isEmpty(searchQuery)) {
            query = query.whereGreaterThanOrEqualTo("name", searchQuery)
                         .whereLessThanOrEqualTo("name", searchQuery + "\uf8ff");
        }
        
        // Apply subject filter
        if (!TextUtils.isEmpty(selectedSubject) && !selectedSubject.equals("All Subjects")) {
            query = query.whereEqualTo("subject", selectedSubject);
        }
        
        // Apply location filter
        if (!TextUtils.isEmpty(selectedLocation) && !selectedLocation.equals("All Locations")) {
            query = query.whereEqualTo("location", selectedLocation);
        }
          // Apply rating filter
        if (minRating > 0) {
            query = query.whereGreaterThanOrEqualTo("ratingAverage", minRating);
        }
        
        // Apply price filter
        query = query.whereLessThanOrEqualTo("pricePerHour", maxPrice);
        
        // Execute query
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchResults.clear();
                    
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        TutorModel tutor = document.toObject(TutorModel.class);
                        if (tutor != null && passesClientSideFilters(tutor)) {
                            tutor.setId(document.getId());
                            searchResults.add(tutor);
                        }
                    }
                    
                    showLoading(false);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error performing search", e);
                    showLoading(false);
                    Toast.makeText(this, "Error performing search", Toast.LENGTH_SHORT).show();
                });
    }
      private boolean passesClientSideFilters(TutorModel tutor) {
        // Check experience filter
        if (!TextUtils.isEmpty(selectedExperience)) {
            String experienceStr = tutor.getExperience();
            // Extract years from experience string (e.g., "5+ years" -> 5)
            int experience = 0;
            if (experienceStr != null) {
                try {
                    // Extract the first number from the experience string
                    String[] parts = experienceStr.split("\\D+");
                    if (parts.length > 0 && !parts[0].isEmpty()) {
                        experience = Integer.parseInt(parts[0]);
                    }
                } catch (NumberFormatException e) {
                    experience = 0; // Default to 0 if parsing fails
                }
            }
            
            switch (selectedExperience) {
                case "0-1 years":
                    if (experience > 1) return false;
                    break;
                case "2-5 years":
                    if (experience < 2 || experience > 5) return false;
                    break;
                case "5-10 years":
                    if (experience < 5 || experience > 10) return false;
                    break;
                case "10+ years":
                    if (experience < 10) return false;
                    break;
            }
        }
          // Check availability type filter
        if (!includeOnline && !includeInPerson) {
            return false; // No availability types selected
        }
        
        if (tutor.getAvailability() != null) {
            // Check if availability map contains online or in-person options
            boolean hasOnline = tutor.getAvailability().containsKey("Online") || 
                               tutor.getAvailability().values().toString().toLowerCase().contains("online");
            boolean hasInPerson = tutor.getAvailability().containsKey("In-person") || 
                                 tutor.getAvailability().values().toString().toLowerCase().contains("in-person");
            
            if (!includeOnline && hasOnline && !hasInPerson) return false;
            if (!includeInPerson && hasInPerson && !hasOnline) return false;
        }
        
        return true;
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        searchResultsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        noResultsText.setVisibility(View.GONE);
    }
    
    private void updateUI() {
        if (searchResults.isEmpty()) {
            searchResultsRecyclerView.setVisibility(View.GONE);
            noResultsText.setVisibility(View.VISIBLE);
            noResultsText.setText("No tutors found matching your criteria");
        } else {
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            noResultsText.setVisibility(View.GONE);
            searchResultsAdapter.notifyDataSetChanged();
        }
        
        // Update toolbar title with result count
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(String.format("Search Results (%d)", searchResults.size()));
        }
    }
}
