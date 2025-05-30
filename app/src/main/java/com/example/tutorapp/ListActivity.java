package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.adapters.TutorListAdapter;
import com.example.tutorapp.models.TutorModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    private AutoCompleteTextView filterSubjectDropdown;
    private TextInputEditText filterLocationEditText;
    private ChipGroup sortChipGroup;
    private TextView resultsCountText;
    private RecyclerView tutorsRecyclerView;
    private View emptyStateLayout;
    private CircularProgressIndicator progressBar;
    
    private TutorListAdapter tutorListAdapter;
    private FirebaseFirestore db;
    private List<TutorModel> allTutors;
    private List<TutorModel> filteredTutors;
    
    private String currentCategory;
    private String currentSortBy = "rating";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Initialize data lists
        allTutors = new ArrayList<>();
        filteredTutors = new ArrayList<>();
        
        // Initialize views
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup filters
        setupFilters();
        
        // Get category from intent
        Intent intent = getIntent();
        currentCategory = intent.getStringExtra("category");
        
        // Load tutors
        loadTutors();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        filterSubjectDropdown = findViewById(R.id.filter_subject_dropdown);
        filterLocationEditText = findViewById(R.id.filter_location_edit_text);
        sortChipGroup = findViewById(R.id.sort_chip_group);
        resultsCountText = findViewById(R.id.results_count_text);
        tutorsRecyclerView = findViewById(R.id.tutors_recycler_view);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Update title based on category
        if (currentCategory != null) {
            toolbar.setTitle(currentCategory + " Tutors");
        }
    }
    
    private void setupRecyclerView() {
        tutorListAdapter = new TutorListAdapter(filteredTutors, this::onTutorClick, this::onFavoriteClick);
        tutorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tutorsRecyclerView.setAdapter(tutorListAdapter);
    }
    
    private void setupFilters() {
        // Setup subject dropdown
        List<String> subjects = Arrays.asList(
            "Mathematics", "Physics", "Chemistry", "Biology", 
            "English", "Computer Science", "History", "Geography"
        );
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, subjects);
        filterSubjectDropdown.setAdapter(subjectAdapter);
        
        // Set default subject if category is provided
        if (currentCategory != null) {
            filterSubjectDropdown.setText(currentCategory, false);
        }
        
        // Setup filter listeners
        filterSubjectDropdown.setOnItemClickListener((parent, view, position, id) -> {
            applyFilters();
        });
        
        filterLocationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });
        
        // Setup sort chip group
        sortChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_rating) {
                    currentSortBy = "rating";
                } else if (checkedId == R.id.chip_price) {
                    currentSortBy = "price";
                } else if (checkedId == R.id.chip_experience) {
                    currentSortBy = "experience";
                }
                applyFilters();
            }
        });
    }
    
    private void loadTutors() {
        showLoading(true);
        
        Query query = db.collection("tutors");
        
        // Filter by category if provided
        if (currentCategory != null) {
            query = query.whereEqualTo("subject", currentCategory);
        }
        
        query.get().addOnCompleteListener(task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                allTutors.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    TutorModel tutor = document.toObject(TutorModel.class);
                    allTutors.add(tutor);
                }
                applyFilters();
            } else {
                Toast.makeText(this, "Error loading tutors", Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }
    
    private void applyFilters() {
        filteredTutors.clear();
        
        String selectedSubject = filterSubjectDropdown.getText().toString().trim();
        String selectedLocation = filterLocationEditText.getText().toString().trim().toLowerCase();
        
        for (TutorModel tutor : allTutors) {
            boolean matchesSubject = selectedSubject.isEmpty() || 
                tutor.getSubject().equalsIgnoreCase(selectedSubject);
            boolean matchesLocation = selectedLocation.isEmpty() || 
                tutor.getLocation().toLowerCase().contains(selectedLocation);
            
            if (matchesSubject && matchesLocation) {
                filteredTutors.add(tutor);
            }
        }
        
        // Sort results
        sortTutors();
        
        // Update UI
        updateResultsCount();
        tutorListAdapter.notifyDataSetChanged();
        showEmptyState(filteredTutors.isEmpty());
    }
    
    private void sortTutors() {
        switch (currentSortBy) {
            case "rating":
                filteredTutors.sort((t1, t2) -> Double.compare(t2.getRatingAverage(), t1.getRatingAverage()));
                break;
            case "price":
                filteredTutors.sort((t1, t2) -> Double.compare(t1.getHourlyRate(), t2.getHourlyRate()));
                break;
            case "experience":
                // For simplicity, sort by name as experience sorting requires parsing
                filteredTutors.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));
                break;
        }
    }
    
    private void updateResultsCount() {
        int count = filteredTutors.size();
        String text = count == 1 ? "Found 1 tutor" : "Found " + count + " tutors";
        resultsCountText.setText(text);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        tutorsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        tutorsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void onTutorClick(TutorModel tutor) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("tutor_id", tutor.getTutorId());
        startActivity(intent);
    }
    
    private void onFavoriteClick(TutorModel tutor) {
        Toast.makeText(this, "Added to favorites: " + tutor.getName(), Toast.LENGTH_SHORT).show();
    }
}
