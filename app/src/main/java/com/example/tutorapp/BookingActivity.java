package com.example.tutorapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.example.tutorapp.models.BookingModel;
import com.example.tutorapp.utils.AuthenticationManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {    private static final String TAG = "BookingActivity";
      private FirebaseFirestore db;
    
    // Intent data
    private String tutorId;
    private String tutorName;
    private String tutorSubject;
    private double tutorPrice;
    
    // UI Components
    private TextView tutorInfoText, sessionDateText, sessionTimeText;
    private TextView subtotalText, taxText, totalText;
    private Button selectDateButton, selectTimeButton, confirmBookingButton;
    private RadioGroup sessionTypeRadioGroup, durationRadioGroup;
    private RadioButton onlineRadio, inPersonRadio;    private RadioButton duration30, duration60, duration90, duration120;
    private TextInputEditText notesEditText, addressEditText;    private LinearLayout addressInputLayout;
    private LinearLayout pricingSummaryCard;
    private ProgressBar progressBar;
    private LinearLayout bookingForm;
    
    // Booking data
    private Calendar selectedDate;
    private Calendar selectedTime;
    private int sessionDuration = 60; // Default 1 hour
    private String sessionType = "online"; // Default online
    private double subtotal = 0.0;
    private double tax = 0.0;
    private double total = 0.0;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        
        // Check authentication first
        if (!AuthenticationManager.isLoggedIn(this)) {
            Toast.makeText(this, "Please sign in to book a session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
          // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Get data from intent
        getIntentData();
        
        initializeViews();
        setupToolbar();
        setupRadioGroups();
        setupButtons();
        updateTutorInfo();
        calculatePricing();
    }
      private void getIntentData() {
        Intent intent = getIntent();
        tutorId = intent.getStringExtra("tutor_id");
        tutorName = intent.getStringExtra("tutor_name");
        tutorSubject = intent.getStringExtra("tutor_subject");
        tutorPrice = intent.getDoubleExtra("tutor_price", 0.0);
        
        if (TextUtils.isEmpty(tutorId)) {
            // If no tutor data provided, show user's bookings list instead
            Toast.makeText(this, "Showing your bookings", Toast.LENGTH_SHORT).show();
            showUserBookings();
            return;
        }
    }
    
    private void initializeViews() {
        tutorInfoText = findViewById(R.id.tutorInfoText);
        sessionDateText = findViewById(R.id.sessionDateText);
        sessionTimeText = findViewById(R.id.sessionTimeText);
        selectDateButton = findViewById(R.id.selectDateButton);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        sessionTypeRadioGroup = findViewById(R.id.sessionTypeRadioGroup);
        onlineRadio = findViewById(R.id.onlineRadio);
        inPersonRadio = findViewById(R.id.inPersonRadio);
        durationRadioGroup = findViewById(R.id.durationRadioGroup);
        duration30 = findViewById(R.id.duration30);
        duration60 = findViewById(R.id.duration60);
        duration90 = findViewById(R.id.duration90);
        duration120 = findViewById(R.id.duration120);
        notesEditText = findViewById(R.id.notesEditText);
        addressEditText = findViewById(R.id.addressEditText);
        addressInputLayout = findViewById(R.id.addressInputLayout);
        subtotalText = findViewById(R.id.subtotalText);
        taxText = findViewById(R.id.taxText);
        totalText = findViewById(R.id.totalText);
        pricingSummaryCard = findViewById(R.id.pricingSummaryCard);
        confirmBookingButton = findViewById(R.id.confirmBookingButton);
        progressBar = findViewById(R.id.progressBar);
        bookingForm = findViewById(R.id.bookingForm);
    }
      private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Book Session");
            }
            
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }
    
    private void setupRadioGroups() {
        // Session type radio group
        sessionTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.onlineRadio) {
                sessionType = "online";
                addressInputLayout.setVisibility(View.GONE);
            } else if (checkedId == R.id.inPersonRadio) {
                sessionType = "in-person";
                addressInputLayout.setVisibility(View.VISIBLE);
            }
        });
        
        // Duration radio group
        durationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.duration30) {
                sessionDuration = 30;
            } else if (checkedId == R.id.duration60) {
                sessionDuration = 60;
            } else if (checkedId == R.id.duration90) {
                sessionDuration = 90;
            } else if (checkedId == R.id.duration120) {
                sessionDuration = 120;
            }
            calculatePricing();
        });
        
        // Set default selections
        onlineRadio.setChecked(true);
        duration60.setChecked(true);
    }
    
    private void setupButtons() {
        selectDateButton.setOnClickListener(v -> showDatePicker());
        selectTimeButton.setOnClickListener(v -> showTimePicker());
        confirmBookingButton.setOnClickListener(v -> confirmBooking());
    }
    
    private void updateTutorInfo() {
        String tutorInfo = String.format("%s\n%s • $%.0f/hour", 
                                       tutorName, tutorSubject, tutorPrice);
        tutorInfoText.setText(tutorInfo);
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    updateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        
        timePickerDialog.show();
    }
    
    private void updateDateDisplay() {
        if (selectedDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
            sessionDateText.setText(dateFormat.format(selectedDate.getTime()));
            sessionDateText.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateTimeDisplay() {
        if (selectedTime != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            sessionTimeText.setText(timeFormat.format(selectedTime.getTime()));
            sessionTimeText.setVisibility(View.VISIBLE);
        }
    }
    
    private void calculatePricing() {
        // Calculate subtotal based on duration
        subtotal = (tutorPrice * sessionDuration) / 60.0;
        
        // Calculate tax (assuming 8.5% tax rate)
        tax = subtotal * 0.085;
        
        // Calculate total
        total = subtotal + tax;
        
        // Update UI
        subtotalText.setText(String.format("$%.2f", subtotal));
        taxText.setText(String.format("$%.2f", tax));
        totalText.setText(String.format("$%.2f", total));
    }    private void confirmBooking() {
        Log.d(TAG, "confirmBooking: Starting booking confirmation");
        
        // Check authentication before proceeding
        if (!AuthenticationManager.isLoggedIn(this)) {
            Toast.makeText(this, "Please sign in to book a session", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "User authenticated, proceeding with booking");
        testFirestoreConnectivity();
    }
    
    private void testFirestoreConnectivity() {
        Log.d(TAG, "Testing basic Firestore read operation...");
        showLoading(true);
        
        db.collection("tutors")
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "*** FIRESTORE READ TEST: SUCCESS! Firebase is working ***");
                    // Now try the actual booking
                    proceedWithActualBooking();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "*** FIRESTORE READ TEST: FAILED ***", e);
                    showLoading(false);
                    Toast.makeText(this, "Firebase connection failed: " + e.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                });
    }
    
    private void proceedWithActualBooking() {
        Log.d(TAG, "proceedWithActualBooking: Firebase working, proceeding with booking");
        
        if (!validateBookingData()) {
            Log.d(TAG, "confirmBooking: Validation failed");
            showLoading(false);
            return;
        }
          // Create booking object
        Log.d(TAG, "proceedWithActualBooking: Creating booking object");
        BookingModel booking = new BookingModel();
        
        // Set tutor data
        Log.d(TAG, "proceedWithActualBooking: Setting tutor data");
        booking.setTutorID(tutorId);
        booking.setTutorName(tutorName);
        booking.setSubject(tutorSubject);
        booking.setSessionType(sessionType);
        booking.setDuration(sessionDuration);
        booking.setPrice(total);
        booking.setStatus("pending");
        booking.setCreatedAt(new Date());
        
        // Set date and time
        Log.d(TAG, "proceedWithActualBooking: Setting date and time");
        if (selectedDate != null && selectedTime != null) {
            Calendar sessionDateTime = Calendar.getInstance();
            sessionDateTime.set(
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE)
            );
            booking.setSessionDate(sessionDateTime.getTime());
            booking.setStartTime(sessionDateTime.getTime());
            
            Calendar endTime = (Calendar) sessionDateTime.clone();
            endTime.add(Calendar.MINUTE, sessionDuration);
            booking.setEndTime(endTime.getTime());
        }
        
        // Set optional fields
        if (notesEditText != null) {
            String notes = notesEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(notes)) {
                booking.setNotes(notes);
            }
        }
        
        if (sessionType.equals("in-person") && addressEditText != null) {
            String address = addressEditText.getText().toString().trim();
            booking.setAddress(address);
        }        // Set student data from authentication
        Log.d(TAG, "proceedWithActualBooking: Setting student data");
        String currentUserId = AuthenticationManager.getCurrentUserId(this);
        String currentUsername = AuthenticationManager.getCurrentUsername(this);
        
        booking.setStudentID(currentUserId);
        booking.setStudentName(currentUsername != null ? currentUsername : "Unknown User");
        
        // Log booking data
        Log.d(TAG, "proceedWithActualBooking: Booking data: " + booking.toString());
        
        // Add timeout
        android.os.Handler timeoutHandler = new android.os.Handler();        Runnable timeoutRunnable = () -> {
            Log.e(TAG, "TIMEOUT: Firestore booking operation took too long");
            // Try to find the booking anyway (in case write succeeded but callback was lost)
            db.collection("bookings")
                .whereEqualTo("studentID", currentUserId)
                .whereEqualTo("tutorID", booking.getTutorID())
                .whereEqualTo("sessionDate", booking.getSessionDate())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "Booking found in Firestore after timeout, treating as success");
                        String bookingId = querySnapshot.getDocuments().get(0).getId();
                        showLoading(false);
                        if (bookingForm != null) bookingForm.setVisibility(View.GONE);
                        showBookingConfirmation(bookingId);
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Booking timeout. Please try again.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Booking timeout. Please try again.", Toast.LENGTH_LONG).show();
                });
        };
        timeoutHandler.postDelayed(timeoutRunnable, 15000);
        
        // Save to Firestore
        Log.d(TAG, "proceedWithActualBooking: Saving to Firestore");
        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "*** BOOKING SUCCESS! ***");
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    String bookingId = documentReference.getId();
                    Log.d(TAG, "Booking created with ID: " + bookingId);
                    
                    showLoading(false);
                    if (bookingForm != null) {
                        bookingForm.setVisibility(View.GONE);
                    }
                    showBookingConfirmation(bookingId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "*** BOOKING FAILED! ***", e);
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    showLoading(false);
                    Toast.makeText(this, "Booking failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    
    private boolean validateBookingData() {
        // Validate date and time
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a session date", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (selectedTime == null) {
            Toast.makeText(this, "Please select a session time", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Check if selected date/time is in the future
        Calendar sessionDateTime = Calendar.getInstance();
        sessionDateTime.set(
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH),
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE)
        );
        
        if (sessionDateTime.before(Calendar.getInstance())) {
            Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show();
            return false;
        }
          // Validate address for in-person sessions
        if (sessionType.equals("in-person")) {
            if (addressEditText == null) {
                Toast.makeText(this, "Address input not available", Toast.LENGTH_SHORT).show();
                return false;
            }
            String address = addressEditText.getText().toString().trim();
            if (TextUtils.isEmpty(address)) {
                Toast.makeText(this, "Please enter an address for in-person sessions", 
                             Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        
        return true;
    }    private void showLoading(boolean show) {
        Log.d(TAG, "showLoading: " + show);
        
        runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                Log.d(TAG, "Progress bar visibility: " + (show ? "VISIBLE" : "GONE"));
            }
            
            if (confirmBookingButton != null) {
                confirmBookingButton.setEnabled(!show);
                confirmBookingButton.setText(show ? "Processing..." : "Confirm Booking");
                Log.d(TAG, "Confirm button enabled: " + !show);
            }
            
            // Don't automatically show the form when loading ends
            // Let other methods control form visibility explicitly
        });
    }
      private void showUserBookings() {
        // Hide the booking form and show a message about bookings
        bookingForm.setVisibility(View.GONE);
        
        // For now, show a simple message. In a full implementation, 
        // this would display a list of user's existing bookings
        Toast.makeText(this, "Your bookings feature coming soon", Toast.LENGTH_LONG).show();
        
        // Optionally, finish this activity and return to previous screen
        finish();
    }    private void showBookingConfirmation(String bookingId) {
        Log.d(TAG, "showBookingConfirmation: STARTED - Creating confirmation dialog for booking " + bookingId);
        
        // Check if activity is still valid
        if (isFinishing() || isDestroyed()) {
            Log.w(TAG, "Activity is finishing or destroyed, cannot show dialog");
            return;
        }
        
        try {
            Log.d(TAG, "About to create AlertDialog.Builder");
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            Log.d(TAG, "AlertDialog.Builder created successfully");
            
            builder.setTitle("Booking Confirmed!")
                    .setMessage("Your booking request has been submitted successfully.\n\n" +
                               "Booking ID: " + bookingId.substring(0, Math.min(8, bookingId.length())) + "...\n" +
                               "Status: PENDING\n\n" +
                               "Your tutor " + tutorName + " will be notified and will confirm your session shortly.")
                    .setPositiveButton("View My Bookings", (dialog, which) -> {
                        Log.d(TAG, "User clicked View My Bookings");
                        Intent intent = new Intent(BookingActivity.this, UserBookingsActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("OK", (dialog, which) -> {
                        Log.d(TAG, "User clicked OK");
                        finish();
                    })
                    .setCancelable(false);
            
            Log.d(TAG, "Dialog builder configured, creating dialog");
            AlertDialog dialog = builder.create();
            Log.d(TAG, "AlertDialog created successfully, about to show");
            
            dialog.show();
            Log.d(TAG, "Dialog.show() called successfully - DIALOG SHOULD NOW BE VISIBLE");
            
        } catch (Exception e) {
            Log.e(TAG, "CRITICAL ERROR: Failed to show confirmation dialog", e);
            e.printStackTrace();
            // Fallback - show toast and navigate
            Toast.makeText(this, "Booking confirmed! ID: " + bookingId.substring(0, Math.min(8, bookingId.length())), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
