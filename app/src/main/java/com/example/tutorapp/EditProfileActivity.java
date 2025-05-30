package com.example.tutorapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.tutorapp.utils.ThemeManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private CircleImageView profileImageView;
    private MaterialButton changePhotoButton;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText bioInput;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;

    private SharedPreferences sharedPreferences;
    private Uri selectedImageUri;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply stored theme before calling super.onCreate()
        ThemeManager.applyStoredTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("TutorPalPrefs", MODE_PRIVATE);
        
        // Initialize views
        initializeViews();
        
        // Setup activity result launchers
        setupActivityResultLaunchers();
        
        // Load current profile data
        loadProfileData();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        profileImageView = findViewById(R.id.profile_image);
        changePhotoButton = findViewById(R.id.change_photo_button);
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        bioInput = findViewById(R.id.bio_input);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        profileImageView.setImageURI(selectedImageUri);
                        saveImageUri(selectedImageUri.toString());
                    }
                }
            }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void loadProfileData() {
        // Load saved profile data
        String name = sharedPreferences.getString("profile_name", "");
        String email = sharedPreferences.getString("profile_email", "");
        String phone = sharedPreferences.getString("profile_phone", "");
        String bio = sharedPreferences.getString("profile_bio", "");
        String imageUri = sharedPreferences.getString("profile_image_uri", "");

        nameInput.setText(name);
        emailInput.setText(email);
        phoneInput.setText(phone);
        bioInput.setText(bio);

        // Load profile image if exists
        if (!imageUri.isEmpty()) {
            try {
                selectedImageUri = Uri.parse(imageUri);
                profileImageView.setImageURI(selectedImageUri);
            } catch (Exception e) {
                // Handle invalid URI
                profileImageView.setImageResource(R.drawable.ic_person);
            }
        }
    }

    private void setupClickListeners() {
        changePhotoButton.setOnClickListener(v -> checkPermissionAndOpenGallery());
        
        saveButton.setOnClickListener(v -> saveProfile());
        
        cancelButton.setOnClickListener(v -> finish());
    }

    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();

        // Basic validation
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (!email.isEmpty() && !isValidEmail(email)) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        // Save profile data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profile_name", name);
        editor.putString("profile_email", email);
        editor.putString("profile_phone", phone);
        editor.putString("profile_bio", bio);
        editor.apply();

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void saveImageUri(String uri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profile_image_uri", uri);
        editor.apply();
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                    finish();
                    return true;                } else if (itemId == R.id.nav_search) {
                    startActivity(new Intent(EditProfileActivity.this, BrowseSearchActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_bookings) {
                    startActivity(new Intent(EditProfileActivity.this, UserBookingsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    startActivity(new Intent(EditProfileActivity.this, UserMessagesActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                }
                
                return false;
            }        });
        
        // Don't auto-select profile to avoid navigation loop
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
