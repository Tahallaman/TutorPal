package com.example.tutorapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.adapters.BookingAdapter;
import com.example.tutorapp.models.BookingModel;
import com.example.tutorapp.utils.AuthenticationManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display user's bookings without requiring tutor-specific context
 */
public class UserBookingsActivity extends AppCompatActivity {
    private static final String TAG = "UserBookingsActivity";
    
    private FirebaseFirestore db;
    private RecyclerView bookingsRecyclerView;
    private BookingAdapter bookingAdapter;
    private List<BookingModel> bookingsList;
    private TextView emptyStateText;
      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bookings);
        
        // Check authentication first
        if (!AuthenticationManager.isLoggedIn(this)) {
            Toast.makeText(this, "Please sign in to view your bookings", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        db = FirebaseFirestore.getInstance();
        bookingsList = new ArrayList<>();
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadUserBookings();
    }
    
    private void initializeViews() {
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("My Bookings");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(this, bookingsList);
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingsRecyclerView.setAdapter(bookingAdapter);
    }    private void loadUserBookings() {
        // Get current user ID from authentication
        String currentUserId = AuthenticationManager.getCurrentUserId(this);
        
        if (currentUserId == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d(TAG, "Loading bookings for user: " + currentUserId);
        
        // First, let's see all bookings in the database for debugging
        db.collection("bookings")
                .get()
                .addOnSuccessListener(allBookings -> {
                    Log.d(TAG, "Total bookings in database: " + allBookings.size());
                    for (QueryDocumentSnapshot doc : allBookings) {
                        Log.d(TAG, "Booking ID: " + doc.getId() + ", StudentID: " + doc.getString("studentID"));
                    }
                });
          db.collection("bookings")
                .whereEqualTo("studentID", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firestore query successful, found " + queryDocumentSnapshots.size() + " documents");
                    bookingsList.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No bookings found for user");
                        showEmptyState();
                        return;
                    }
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingModel booking = document.toObject(BookingModel.class);
                        booking.setId(document.getId());
                        bookingsList.add(booking);
                        Log.d(TAG, "Added booking: " + booking.getTutorName() + " - " + booking.getSubject());
                    }
                    
                    Log.d(TAG, "Notifying adapter of " + bookingsList.size() + " bookings");
                    bookingAdapter.notifyDataSetChanged();
                    showBookings();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load bookings", e);
                    Toast.makeText(this, "Failed to load bookings: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                    showEmptyState();                });
    }
    
    private void showEmptyState() {
        bookingsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("No bookings found.\nBook a tutor to see your appointments here.");
    }
    
    private void showBookings() {
        bookingsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
    }
}
