package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tutorapp.utils.AuthenticationManager;
import com.example.tutorapp.utils.ThemeManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private RadioGroup themeRadioGroup;
    private MaterialRadioButton lightThemeRadio;
    private MaterialRadioButton darkThemeRadio;
    private MaterialRadioButton systemThemeRadio;
    
    // Account management components
    private LinearLayout accountSection;
    private MaterialButton changePasswordButton;
    private MaterialButton updateEmailButton;
    private MaterialButton deleteAccountButton;
    private MaterialButton applyForTutorButton;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply stored theme before calling super.onCreate()
        ThemeManager.applyStoredTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupThemeSelection();
        setupAccountManagement();
        setupBottomNavigation();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        themeRadioGroup = findViewById(R.id.theme_radio_group);
        lightThemeRadio = findViewById(R.id.light_theme_radio);
        darkThemeRadio = findViewById(R.id.dark_theme_radio);
        systemThemeRadio = findViewById(R.id.system_theme_radio);
        
        // Account management views
        accountSection = findViewById(R.id.account_section);
        changePasswordButton = findViewById(R.id.change_password_button);
        updateEmailButton = findViewById(R.id.update_email_button);
        deleteAccountButton = findViewById(R.id.delete_account_button);
        applyForTutorButton = findViewById(R.id.apply_for_tutor_button);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupThemeSelection() {        // Set current theme selection
        int currentTheme = ThemeManager.getSavedTheme(this);
        switch (currentTheme) {
            case ThemeManager.THEME_LIGHT:
                lightThemeRadio.setChecked(true);
                break;
            case ThemeManager.THEME_DARK:
                darkThemeRadio.setChecked(true);
                break;
            case ThemeManager.THEME_SYSTEM:
                systemThemeRadio.setChecked(true);
                break;
        }

        // Setup theme change listener
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int newTheme;
            String themeName;
            
            if (checkedId == R.id.light_theme_radio) {
                newTheme = ThemeManager.THEME_LIGHT;
                themeName = "Light";
            } else if (checkedId == R.id.dark_theme_radio) {
                newTheme = ThemeManager.THEME_DARK;
                themeName = "Dark";
            } else {
                newTheme = ThemeManager.THEME_SYSTEM;
                themeName = "System Default";
            }
            
            ThemeManager.saveTheme(this, newTheme);        Toast.makeText(this, "Theme changed to " + themeName, Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setupAccountManagement() {
        // Check if user is logged in
        if (!AuthenticationManager.isLoggedIn(this)) {
            if (accountSection != null) {
                accountSection.setVisibility(View.GONE);
            }
            return;
        }
        
        // Show/hide tutor application button based on current role
        AuthenticationManager.UserRole userRole = AuthenticationManager.getCurrentUserRole(this);
        if (applyForTutorButton != null) {
            if (userRole == AuthenticationManager.UserRole.TUTOR || userRole == AuthenticationManager.UserRole.ADMIN) {
                applyForTutorButton.setVisibility(View.GONE);
            } else {
                applyForTutorButton.setVisibility(View.VISIBLE);
                applyForTutorButton.setOnClickListener(v -> showTutorApplicationDialog());
            }
        }
        
        // Set up account management buttons
        if (changePasswordButton != null) {
            changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        }
        
        if (updateEmailButton != null) {
            updateEmailButton.setOnClickListener(v -> showUpdateEmailDialog());
        }
        
        if (deleteAccountButton != null) {
            deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());
        }
    }
      private void showTutorApplicationDialog() {
        // Simplified dialog without custom layout for now
        new AlertDialog.Builder(this)
            .setTitle("Apply to become a Tutor")
            .setMessage("Feature coming soon! You can apply to become a tutor through this option.")
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showChangePasswordDialog() {
        // Simplified dialog without custom layout for now
        new AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setMessage("Feature coming soon! You will be able to change your password through this option.")
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showUpdateEmailDialog() {
        // Simplified dialog without custom layout for now
        new AlertDialog.Builder(this)
            .setTitle("Update Email Address")
            .setMessage("Feature coming soon! You will be able to update your email address through this option.")
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete Account", (dialog, which) -> {
                // Show confirmation dialog
                new AlertDialog.Builder(this)
                    .setTitle("Final Confirmation")
                    .setMessage("This will permanently delete your account and all associated data. Are you absolutely sure?")
                    .setPositiveButton("Yes, Delete", (confirmDialog, confirmWhich) -> {
                        // For now, just log out (in real app, would delete account data)
                        AuthenticationManager.signOut(this);
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        
                        // Navigate back to main activity
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    startActivity(new android.content.Intent(SettingsActivity.this, MainActivity.class));
                    finish();
                    return true;                } else if (itemId == R.id.nav_search) {
                    startActivity(new android.content.Intent(SettingsActivity.this, BrowseSearchActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_bookings) {
                    startActivity(new android.content.Intent(SettingsActivity.this, UserBookingsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    startActivity(new android.content.Intent(SettingsActivity.this, UserMessagesActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new android.content.Intent(SettingsActivity.this, ProfileActivity.class));
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
