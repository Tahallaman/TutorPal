package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.adapters.TutorListAdapter;
import com.example.tutorapp.models.TutorModel;
import com.example.tutorapp.utils.FavouritesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display user's favorite tutors in a scrollable list
 */
public class FavouritesActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private CircularProgressIndicator progressIndicator;
    private MaterialButton browseTutorsButton;
    
    private TutorListAdapter adapter;
    private List<TutorModel> favoriteTutors;
    private FavouritesManager favouritesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
          // Initialize views
        initializeViews();
        
        // Initialize favorites manager
        favouritesManager = new FavouritesManager(this);
        
        // Setup toolbar
        setupToolbar();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Load favorite tutors
        loadFavoriteTutors();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);        recyclerView = findViewById(R.id.favourites_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        progressIndicator = findViewById(R.id.progress_indicator);
        browseTutorsButton = findViewById(R.id.browse_tutors_button);
          // Setup browse tutors button
        browseTutorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, BrowseSearchActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Favourites");
        }
    }
      private void setupRecyclerView() {
        favoriteTutors = new ArrayList<>();
        adapter = new TutorListAdapter(favoriteTutors, this::onTutorClick, this::onFavoriteClick);
        adapter.setFavouritesManager(favouritesManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(FavouritesActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;                } else if (itemId == R.id.nav_search) {
                    Intent intent = new Intent(FavouritesActivity.this, BrowseSearchActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_bookings) {
                    Intent intent = new Intent(FavouritesActivity.this, UserBookingsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    Intent intent = new Intent(FavouritesActivity.this, UserMessagesActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(FavouritesActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                
                return false;
            }
        });
        
        // Don't set any item as selected since this is not a main navigation destination
    }
    
    private void loadFavoriteTutors() {
        showLoading(true);
        
        favouritesManager.loadFavoriteTutors(new FavouritesManager.FavouritesListCallback() {            @Override
            public void onSuccess(List<TutorModel> tutors) {
                showLoading(false);
                favoriteTutors.clear();
                favoriteTutors.addAll(tutors);
                adapter.notifyDataSetChanged();
                
                updateEmptyState();
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                
                // Check if we have favorites but couldn't load details (offline scenario)
                int favoriteCount = favouritesManager.getFavoriteCount();
                if (favoriteCount > 0) {
                    // Show offline message instead of error
                    Toast.makeText(FavouritesActivity.this, 
                        "You have " + favoriteCount + " favorites, but details unavailable offline", 
                        Toast.LENGTH_LONG).show();
                } else {
                    // Show actual error if no favorites exist
                    Toast.makeText(FavouritesActivity.this, error, Toast.LENGTH_LONG).show();
                }
                
                updateEmptyState();
            }
        });
    }
      private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        findViewById(R.id.empty_state_layout).setVisibility(View.GONE);
    }    private void updateEmptyState() {
        int favoriteCount = favouritesManager.getFavoriteCount();
        
        if (favoriteTutors.isEmpty()) {
            if (favoriteCount > 0) {
                // We have favorites but couldn't load details (offline)
                recyclerView.setVisibility(View.GONE);
                findViewById(R.id.empty_state_layout).setVisibility(View.VISIBLE);
                // Update empty state text to show offline message
                emptyStateText.setText("You have " + favoriteCount + " favorites.\nConnect to internet to view details.");
            } else {
                // No favorites at all
                recyclerView.setVisibility(View.GONE);
                findViewById(R.id.empty_state_layout).setVisibility(View.VISIBLE);
                emptyStateText.setText("No favorites yet!\nStart adding tutors to your favorites list.");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.empty_state_layout).setVisibility(View.GONE);
        }
    }
    
    private void onTutorClick(TutorModel tutor) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("tutor_id", tutor.getTutorId());
        startActivity(intent);
    }
    
    private void onFavoriteClick(TutorModel tutor) {
        // Remove from favorites
        favouritesManager.removeFromFavorites(tutor.getTutorId(), new FavouritesManager.FavouritesCallback() {
            @Override
            public void onSuccess() {
                // Remove from local list and update adapter
                favoriteTutors.remove(tutor);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                
                Toast.makeText(FavouritesActivity.this, 
                    "Removed " + tutor.getName() + " from favorites", 
                    Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(FavouritesActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites when returning to this activity
        loadFavoriteTutors();
    }
}
