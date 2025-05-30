package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.adapters.CategoryAdapter;
import com.example.tutorapp.adapters.TutorListAdapter;
import com.example.tutorapp.models.TutorModel;
import com.example.tutorapp.utils.AuthenticationManager;
import com.example.tutorapp.utils.AuthUtils;
import com.example.tutorapp.utils.DummyDataGenerator;
import com.example.tutorapp.utils.FavouritesManager;
import com.example.tutorapp.utils.ThemeManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private SearchView searchView;
    private MaterialButton signInButton;
    private MaterialButton languagesButton;
    private MaterialButton academicButton;
    private MaterialButton musicButton;
    private MaterialButton browseTutorsButton;
    private MaterialButton advancedSearchButton;
    private FloatingActionButton fabChat;
    private RecyclerView featuredTutorsRecyclerView;
    private BottomNavigationView bottomNavigation;
      private TutorListAdapter tutorListAdapter;
    private FirebaseFirestore db;
    private FavouritesManager favouritesManager;
    
    private List<TutorModel> featuredTutors;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply stored theme before calling super.onCreate()
        ThemeManager.applyStoredTheme(this);
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Initialize favorites manager
        favouritesManager = new FavouritesManager(this);
        
        // Sync user session with Firestore on app startup (if logged in)
        if (AuthenticationManager.isLoggedIn(this)) {
            AuthenticationManager.syncUserSession(this, null); // Silent sync
        }
        
        // Seed dummy data on first launch
        seedDataIfNeeded();
          // Initialize views
        initializeViews();
        
        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Load data
        loadFeaturedTutors();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
      private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
        signInButton = findViewById(R.id.btn_sign_in);
        languagesButton = findViewById(R.id.btn_languages);
        academicButton = findViewById(R.id.btn_academic);
        musicButton = findViewById(R.id.btn_music);
        browseTutorsButton = findViewById(R.id.myBookingsButton);
        advancedSearchButton = findViewById(R.id.btn_advanced_search);
        fabChat = findViewById(R.id.fab_chat);
        featuredTutorsRecyclerView = findViewById(R.id.featuredTutorsRecyclerView);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        setSupportActionBar(toolbar);
    }    private void setupRecyclerViews() {
        // Featured Tutors RecyclerView
        featuredTutors = new ArrayList<>();
        tutorListAdapter = new TutorListAdapter(featuredTutors, this::onTutorClick, this::onFavoriteClick);
        tutorListAdapter.setFavouritesManager(favouritesManager);
        featuredTutorsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredTutorsRecyclerView.setAdapter(tutorListAdapter);
    }
      private void setupClickListeners() {
        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });        // Sign In button
        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Category buttons
        languagesButton.setOnClickListener(v -> navigateToCategory("Languages"));
        academicButton.setOnClickListener(v -> navigateToCategory("Academic"));
        musicButton.setOnClickListener(v -> navigateToCategory("Music"));
          // Browse all tutors button
        browseTutorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BrowseSearchActivity.class);
            startActivity(intent);
        });
          // Advanced search button
        advancedSearchButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BrowseSearchActivity.class);
            startActivity(intent);
        });
          // Floating action button for chat
        fabChat.setOnClickListener(v -> {
            // Check authentication before allowing access to messages
            if (!AuthUtils.requireAuthentication(this, "access messages")) {
                return;
            }
            
            Intent intent = new Intent(MainActivity.this, UserMessagesActivity.class);
            startActivity(intent);
        });}
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    // Already on home, do nothing
                    return true;                } else if (itemId == R.id.nav_search) {
                    Intent intent = new Intent(MainActivity.this, BrowseSearchActivity.class);
                    startActivity(intent);
                    return true;                } else if (itemId == R.id.nav_bookings) {                    // Check authentication before allowing access to bookings
                    if (!AuthUtils.requireAuthentication(MainActivity.this, "view your bookings")) {
                        return false;
                    }
                    
                    Intent intent = new Intent(MainActivity.this, UserBookingsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    // Check authentication before allowing access to messages
                    if (!AuthUtils.requireAuthentication(MainActivity.this, "access messages")) {
                        return false;
                    }
                    
                    Intent intent = new Intent(MainActivity.this, UserMessagesActivity.class);
                    startActivity(intent);
                    return true;} else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                
                return false;
            }
        });
        
        // Set home as selected
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
      private void navigateToCategory(String category) {
        Intent intent = new Intent(MainActivity.this, BrowseSearchActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }    private void loadFeaturedTutors() {
        db.collection("tutors")
            .whereGreaterThan("ratingAverage", 4.5)
            .limit(5)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    featuredTutors.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        TutorModel tutor = document.toObject(TutorModel.class);
                        
                        // Ensure tutorId is set from document ID if it's null or empty
                        // This is essential for proper navigation to DetailsActivity
                        if (tutor.getTutorId() == null || tutor.getTutorId().isEmpty()) {
                            tutor.setTutorId(document.getId());
                        }
                        
                        featuredTutors.add(tutor);
                    }
                    tutorListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Error loading featured tutors", Toast.LENGTH_SHORT).show();
                    Log.e("MainActivity", "Error loading featured tutors", task.getException());
                }
            });
    }
      private void performSearch(String query) {
        if (!query.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, BrowseSearchActivity.class);
            intent.putExtra("search_query", query);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
        }
    }    private void onTutorClick(TutorModel tutor) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("tutor_id", tutor.getTutorId());
        startActivity(intent);
    }private void onFavoriteClick(TutorModel tutor) {
        // Toggle favorite functionality using FavouritesManager
        favouritesManager.toggleFavorite(tutor.getTutorId(), new FavouritesManager.FavouritesCallback() {
            @Override
            public void onSuccess() {
                boolean isFavorite = favouritesManager.isFavorite(tutor.getTutorId());
                String message = isFavorite 
                    ? "Added " + tutor.getName() + " to favorites"
                    : "Removed " + tutor.getName() + " from favorites";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                
                // Update the adapter to refresh favorite button states
                tutorListAdapter.notifyDataSetChanged();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });    }    private void seedDataIfNeeded() {
        // Check if data already exists
        db.collection("tutors").limit(1).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().isEmpty()) {
                    // No tutors found, seed the data
                    DummyDataGenerator.generateDummyTutors();
                    DummyDataGenerator.generateDummyCategories();
                    
                    // Generate reviews after a short delay to ensure tutors are created first
                    new android.os.Handler().postDelayed(() -> {
                        DummyDataGenerator.generateDummyReviews();
                        Toast.makeText(this, "Sample data and reviews loaded", Toast.LENGTH_SHORT).show();
                    }, 2000); // 2 second delay
                }
            });
    }
}

