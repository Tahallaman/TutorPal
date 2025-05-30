package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tutorapp.models.UserModel;
import com.example.tutorapp.utils.AuthenticationManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class AccountCreationActivity extends AppCompatActivity {
    private static final String TAG = "AccountCreationActivity";
    
    private FirebaseFirestore db;
    
    // UI Components
    private Toolbar toolbar;
    private TextInputLayout fullNameInputLayout, usernameInputLayout, emailInputLayout;
    private TextInputLayout passwordInputLayout, confirmPasswordInputLayout;
    private TextInputLayout phoneInputLayout, locationInputLayout, bioInputLayout;
    private TextInputEditText fullNameInput, usernameInput, emailInput;
    private TextInputEditText passwordInput, confirmPasswordInput;
    private TextInputEditText phoneInput, locationInput, bioInput;
    private MaterialButton createAccountButton;
    private ProgressBar progressBar;
    private TextView signInLink;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);
        
        Log.d(TAG, "AccountCreationActivity started");
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        initializeViews();
        setupClickListeners();
        setupToolbar();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        
        // Account details inputs
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        
        fullNameInput = findViewById(R.id.fullNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        
        // Personal information inputs
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        locationInputLayout = findViewById(R.id.locationInputLayout);
        bioInputLayout = findViewById(R.id.bioInputLayout);
        
        phoneInput = findViewById(R.id.phoneInput);
        locationInput = findViewById(R.id.locationInput);
        bioInput = findViewById(R.id.bioInput);
        
        // Action components
        createAccountButton = findViewById(R.id.createAccountButton);
        progressBar = findViewById(R.id.progressBar);
        signInLink = findViewById(R.id.signInLink);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupClickListeners() {
        createAccountButton.setOnClickListener(v -> createAccount());
        
        signInLink.setOnClickListener(v -> {
            Intent intent = new Intent(AccountCreationActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void createAccount() {
        Log.d(TAG, "Create account button clicked");
        
        // Clear previous errors
        clearErrors();
        
        // Get input values
        String fullName = getTextFromInput(fullNameInput);
        String username = getTextFromInput(usernameInput);
        String email = getTextFromInput(emailInput);
        String password = getTextFromInput(passwordInput);
        String confirmPassword = getTextFromInput(confirmPasswordInput);
        String phone = getTextFromInput(phoneInput);
        String location = getTextFromInput(locationInput);
        String bio = getTextFromInput(bioInput);
        
        // Validate inputs
        if (!validateInputs(fullName, username, email, password, confirmPassword, location)) {
            return;
        }
        
        showLoading(true);
        
        // Create user model
        UserModel newUser = new UserModel();
        newUser.setName(fullName.trim());
        newUser.setUserId(username.toLowerCase().trim()); // Use username as initial ID
        newUser.setPassword(password); // In production, hash this
        newUser.setLocation(location.trim());
        newUser.setRole(Arrays.asList("REGISTERED_USER"));
        
        // Add optional fields if provided
        if (!TextUtils.isEmpty(phone)) {
            // Note: UserModel might need a phone field - for now we'll skip it
            Log.d(TAG, "Phone number provided: " + phone);
        }
        
        if (!TextUtils.isEmpty(bio)) {
            // Note: UserModel might need a bio field - for now we'll skip it
            Log.d(TAG, "Bio provided: " + bio);
        }
        
        // Create account in Firestore
        createAccountInFirestore(newUser, email, username);
    }
    
    private void createAccountInFirestore(UserModel user, String email, String username) {
        Log.d(TAG, "Creating account in Firestore for user: " + username);
        
        // Check if username already exists
        db.collection("users")
                .whereEqualTo("name", username.trim())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            showLoading(false);
                            showError(usernameInputLayout, "Username already exists");
                            Log.w(TAG, "Username already exists: " + username);
                            return;
                        }
                        
                        // Username is available, create the account
                        db.collection("users")
                                .add(user)
                                .addOnSuccessListener(documentReference -> {
                                    String userId = documentReference.getId();
                                    user.setUserId(userId);
                                    
                                    Log.d(TAG, "Account created successfully with ID: " + userId);
                                    
                                    // Sign in the user automatically
                                    AuthenticationManager.saveUserSession(AccountCreationActivity.this, user);
                                    
                                    showLoading(false);
                                    Toast.makeText(this, "Account created successfully! Welcome to TutorPal!", Toast.LENGTH_LONG).show();
                                    
                                    // Navigate to main activity
                                    Intent intent = new Intent(AccountCreationActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    showLoading(false);
                                    Log.e(TAG, "Error creating account in Firestore", e);
                                    Toast.makeText(this, "Account creation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        showLoading(false);
                        Log.e(TAG, "Error checking username availability", task.getException());
                        Toast.makeText(this, "Error checking username availability", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private boolean validateInputs(String fullName, String username, String email, 
                                   String password, String confirmPassword, String location) {
        boolean isValid = true;
        
        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            showError(fullNameInputLayout, "Full name is required");
            isValid = false;
        }
        
        // Validate username
        if (TextUtils.isEmpty(username)) {
            showError(usernameInputLayout, "Username is required");
            isValid = false;
        } else if (username.length() < 3) {
            showError(usernameInputLayout, "Username must be at least 3 characters");
            isValid = false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showError(usernameInputLayout, "Username can only contain letters, numbers, and underscores");
            isValid = false;
        }
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            showError(emailInputLayout, "Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(emailInputLayout, "Please enter a valid email address");
            isValid = false;
        }
        
        // Validate password
        if (TextUtils.isEmpty(password)) {
            showError(passwordInputLayout, "Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            showError(passwordInputLayout, "Password must be at least 6 characters");
            isValid = false;
        }
        
        // Validate password confirmation
        if (TextUtils.isEmpty(confirmPassword)) {
            showError(confirmPasswordInputLayout, "Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            showError(confirmPasswordInputLayout, "Passwords do not match");
            isValid = false;
        }
        
        // Validate location
        if (TextUtils.isEmpty(location)) {
            showError(locationInputLayout, "Location is required");
            isValid = false;
        }
        
        return isValid;
    }
    
    private String getTextFromInput(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
    
    private void showError(TextInputLayout layout, String error) {
        layout.setError(error);
        layout.setErrorEnabled(true);
    }
    
    private void clearErrors() {
        fullNameInputLayout.setErrorEnabled(false);
        usernameInputLayout.setErrorEnabled(false);
        emailInputLayout.setErrorEnabled(false);
        passwordInputLayout.setErrorEnabled(false);
        confirmPasswordInputLayout.setErrorEnabled(false);
        phoneInputLayout.setErrorEnabled(false);
        locationInputLayout.setErrorEnabled(false);
        bioInputLayout.setErrorEnabled(false);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        createAccountButton.setEnabled(!show);
        
        if (show) {
            createAccountButton.setText("Creating Account...");
        } else {
            createAccountButton.setText("Create Account");
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
