package com.example.tutorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tutorapp.utils.AuthenticationManager;
import com.example.tutorapp.utils.ThemeManager;
import com.example.tutorapp.models.UserModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private MaterialCardView loginCard;
    private MaterialCardView profileCard;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;    private MaterialButton loginButton;
    private MaterialButton devLoginButton;
    private MaterialButton createAccountButton;
    private MaterialButton logoutButton;private MaterialButton editProfileButton;
    private MaterialButton favouritesButton;
    private MaterialButton settingsButton;
    private MaterialButton adminButton;
    
    // Profile display elements
    private CircleImageView profileImageView;
    private TextView profileNameText;
    private TextView profileEmailText;
    
    private SharedPreferences sharedPreferences;
    private boolean isLoggedIn = false;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply stored theme before calling super.onCreate()
        ThemeManager.applyStoredTheme(this);
        
        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_profile);
        
        // Initialize SharedPreferences - use same prefs as AuthenticationManager
        sharedPreferences = getSharedPreferences("TutorPalAuthPrefs", MODE_PRIVATE);
        
        // Initialize views
        initializeViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Check login status
        checkLoginStatus();
    }
      private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        loginCard = findViewById(R.id.login_card);
        profileCard = findViewById(R.id.profile_card);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        devLoginButton = findViewById(R.id.dev_login_button);
        createAccountButton = findViewById(R.id.create_account_button);
        logoutButton = findViewById(R.id.logout_button);
        editProfileButton = findViewById(R.id.edit_profile_button);
        favouritesButton = findViewById(R.id.favourites_button);
        settingsButton = findViewById(R.id.settings_button);
        adminButton = findViewById(R.id.admin_button);
        
        // Profile display elements
        profileImageView = findViewById(R.id.profile_image_display);
        profileNameText = findViewById(R.id.profile_name_text);
        profileEmailText = findViewById(R.id.profile_email_text);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        devLoginButton.setOnClickListener(v -> performDevLogin());
        createAccountButton.setOnClickListener(v -> openCreateAccount());
        logoutButton.setOnClickListener(v -> performLogout());
        editProfileButton.setOnClickListener(v -> editProfile());
        favouritesButton.setOnClickListener(v -> openFavourites());
        settingsButton.setOnClickListener(v -> openSettings());
        adminButton.setOnClickListener(v -> openAdminPanel());
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;                } else if (itemId == R.id.nav_search) {
                    Intent intent = new Intent(ProfileActivity.this, BrowseSearchActivity.class);
                    startActivity(intent);
                    finish();
                    return true;                } else if (itemId == R.id.nav_bookings) {
                    // Check authentication before allowing access to bookings
                    if (!AuthenticationManager.isLoggedIn(ProfileActivity.this)) {
                        Toast.makeText(ProfileActivity.this, "Please sign in to view your bookings", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                    Intent intent = new Intent(ProfileActivity.this, UserBookingsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    // Check authentication before allowing access to messages
                    if (!AuthenticationManager.isLoggedIn(ProfileActivity.this)) {
                        Toast.makeText(ProfileActivity.this, "Please sign in to access messages", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                    Intent intent = new Intent(ProfileActivity.this, UserMessagesActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Already on profile, do nothing
                    return true;
                }
                
                return false;
            }
        });
        
        // Set profile as selected
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }    private void checkLoginStatus() {
        isLoggedIn = AuthenticationManager.isLoggedIn(this);
        
        if (isLoggedIn) {
            // Sync user session with Firestore to ensure data consistency across devices
            AuthenticationManager.syncUserSession(this, new AuthenticationManager.AuthCallback() {
                @Override
                public void onSuccess(UserModel user) {
                    // Session synced successfully, update UI
                    updateUI();
                }
                
                @Override
                public void onError(String error) {
                    // Sync failed, user might have been deleted or session invalid
                    Toast.makeText(ProfileActivity.this, "Session sync failed: " + error, Toast.LENGTH_SHORT).show();
                    // Force logout and update UI
                    AuthenticationManager.signOut(ProfileActivity.this);
                    isLoggedIn = false;
                    updateUI();
                }
            });
        } else {
            updateUI();
        }
    }private void updateUI() {
        if (isLoggedIn) {
            loginCard.setVisibility(android.view.View.GONE);
            profileCard.setVisibility(android.view.View.VISIBLE);
              // Load and display profile information
            loadProfileInfo();
            
            // Show admin button only for admin users or demo developers
            AuthenticationManager.UserRole role = AuthenticationManager.getCurrentUserRole(this);
            if (role == AuthenticationManager.UserRole.ADMIN || 
                "developer".equals(AuthenticationManager.getCurrentUsername(this))) {
                adminButton.setVisibility(android.view.View.VISIBLE);
            } else {
                adminButton.setVisibility(android.view.View.GONE);
            }
        } else {
            loginCard.setVisibility(android.view.View.VISIBLE);
            profileCard.setVisibility(android.view.View.GONE);
        }
    }    private void performLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use AuthenticationManager for login with proper callback pattern
        AuthenticationManager.signIn(this, username, password, new AuthenticationManager.AuthCallback() {
            @Override
            public void onSuccess(UserModel user) {
                isLoggedIn = true;
                updateUI();
                usernameInput.setText("");
                passwordInput.setText("");
                Toast.makeText(ProfileActivity.this, "Login successful! Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }private void performDevLogin() {
        // Developer bypass login for testing
        try {
            AuthenticationManager.signInDemoUser(this, "developer", AuthenticationManager.UserRole.ADMIN);
            isLoggedIn = true;
            updateUI();
            Toast.makeText(this, "Developer login successful!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Developer login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openCreateAccount() {
        Intent intent = new Intent(this, AccountCreationActivity.class);
        startActivity(intent);
    }
    
    private void performLogout() {
        AuthenticationManager.signOut(this);
        isLoggedIn = false;
        updateUI();
        usernameInput.setText("");
        passwordInput.setText("");
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }    private void editProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }
    
    private void openFavourites() {
        Intent intent = new Intent(this, FavouritesActivity.class);
        startActivity(intent);
    }    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
      private void loadProfileInfo() {
        // Load profile data from AuthenticationManager
        String name = AuthenticationManager.getCurrentUsername(this);
        String email = AuthenticationManager.getCurrentUserEmail(this);
        AuthenticationManager.UserRole role = AuthenticationManager.getCurrentUserRole(this);

        // Update profile display
        if (profileNameText != null) {
            profileNameText.setText(name != null && !name.isEmpty() ? "Hello, " + name + "!" : "Welcome back!");
        }
        
        if (profileEmailText != null) {
            if (email != null && !email.isEmpty()) {
                profileEmailText.setText(email);
                profileEmailText.setVisibility(android.view.View.VISIBLE);
            } else {
                String roleText = role != null ? role.toString().toLowerCase().replace("_", " ") : "user";
                profileEmailText.setText("Logged in as " + roleText);
                profileEmailText.setVisibility(android.view.View.VISIBLE);
            }
        }

        // Set default profile image
        if (profileImageView != null) {
            profileImageView.setImageResource(R.drawable.ic_person);
        }
    }
    
    private void openAdminPanel() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile info when returning from edit profile
        if (isLoggedIn) {
            loadProfileInfo();
        }
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
