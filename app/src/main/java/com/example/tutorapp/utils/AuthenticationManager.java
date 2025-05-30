package com.example.tutorapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.tutorapp.models.UserModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;

/**
 * Comprehensive authentication manager supporting user roles and complete user management
 */
public class AuthenticationManager {
    private static final String TAG = "AuthenticationManager";
    private static final String PREFS_NAME = "TutorPalAuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_IS_TUTOR = "is_tutor";
    private static final String KEY_EMAIL = "email";
    
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    public enum UserRole {
        GUEST, REGISTERED_USER, TUTOR, ADMIN
    }
    
    public interface AuthCallback {
        void onSuccess(UserModel user);
        void onError(String error);
    }
    
    public interface RegisterCallback {
        void onSuccess(UserModel user);
        void onError(String error);
    }    /**
     * Create a test user for debugging purposes - now properly synced with Firestore
     */
    public static void signInTestUser(Context context) {
        Log.d(TAG, "Creating test user for debugging");
        
        String testUsername = "testuser_" + System.currentTimeMillis();
        
        UserModel testUser = new UserModel();
        testUser.setName(testUsername);
        testUser.setPassword("test123");
        testUser.setRole(Arrays.asList("REGISTERED_USER"));
        testUser.setLocation("Test Location");
        
        // Save to Firestore
        db.collection("users")
                .add(testUser)
                .addOnSuccessListener(documentReference -> {
                    testUser.setUserId(documentReference.getId());
                    saveUserSession(context, testUser);
                    Log.d(TAG, "Test user created and logged in: " + testUser.getName() + " (ID: " + testUser.getUserId() + ")");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create test user in Firestore", e);
                });
    }/**
     * Sign in demo user for development testing - now properly synced with Firestore
     */
    public static void signInDemoUser(Context context, String username, UserRole role) {
        Log.d(TAG, "Signing in demo user: " + username + " with role: " + role);
        
        // Check if demo user already exists in Firestore
        db.collection("users")
                .whereEqualTo("name", username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Demo user exists, sign them in
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        UserModel existingUser = document.toObject(UserModel.class);
                        if (existingUser != null) {
                            existingUser.setUserId(document.getId());
                            saveUserSession(context, existingUser);
                            Log.d(TAG, "Existing demo user signed in: " + existingUser.getName());
                        }
                    } else {
                        // Create new demo user in Firestore
                        UserModel demoUser = new UserModel();
                        demoUser.setName(username);
                        demoUser.setPassword(username); // Simple password for demo
                        demoUser.setLocation("Demo Location");
                        
                        // Set role based on the enum
                        switch (role) {
                            case ADMIN:
                                demoUser.setRole(Arrays.asList("ADMIN", "REGISTERED_USER"));
                                break;
                            case TUTOR:
                                demoUser.setRole(Arrays.asList("TUTOR", "REGISTERED_USER"));
                                break;
                            case REGISTERED_USER:
                            default:
                                demoUser.setRole(Arrays.asList("REGISTERED_USER"));
                                break;
                        }
                        
                        // Save to Firestore
                        db.collection("users")
                                .add(demoUser)
                                .addOnSuccessListener(documentReference -> {
                                    demoUser.setUserId(documentReference.getId());
                                    saveUserSession(context, demoUser);
                                    Log.d(TAG, "New demo user created and signed in: " + demoUser.getName() + " (ID: " + demoUser.getUserId() + ")");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create demo user in Firestore", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check for existing demo user", e);
                });
    }
    
    /**
     * Sign in user with username and password
     */
    public static void signIn(Context context, String username, String password, AuthCallback callback) {
        Log.d(TAG, "Sign in attempt for username: " + username);
        
        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Sign in failed: Username is empty");
            callback.onError("Username cannot be empty");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            Log.e(TAG, "Sign in failed: Password is empty");
            callback.onError("Password cannot be empty");
            return;
        }        // Check for demo users first
        if (handleDemoUsers(context, username, password, callback)) {
            Log.d(TAG, "Demo user login successful for: " + username);
            return;
        }
          Log.d(TAG, "Checking Firestore for user: " + username);
        
        // Query Firestore for user by name field (which serves as username)
        db.collection("users")
                .whereEqualTo("name", username.trim())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firestore query completed. Found " + queryDocumentSnapshots.size() + " users");
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w(TAG, "No user found with username: " + username);
                        callback.onError("Invalid username or password");
                        return;
                    }
                    
                    DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);
                    UserModel user = userDocument.toObject(UserModel.class);
                    
                    if (user == null) {
                        Log.e(TAG, "Failed to parse user document for username: " + username);
                        callback.onError("Error loading user data");
                        return;
                    }
                    
                    // Set the userId from the document ID
                    user.setUserId(userDocument.getId());
                    
                    // Verify password (in production, use proper password hashing)
                    if (user.getPassword() != null && user.getPassword().equals(password)) {
                        Log.d(TAG, "User found and authenticated: " + user.getName() + " (ID: " + user.getUserId() + ")");
                        saveUserSession(context, user);
                        callback.onSuccess(user);
                    } else {
                        Log.w(TAG, "Invalid password for user: " + username);
                        callback.onError("Invalid username or password");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying Firestore for user: " + e.getMessage(), e);
                    if (e.getCause() != null) {
                        Log.e(TAG, "Exception cause: " + e.getCause().getMessage());
                    }
                    callback.onError("Login failed: " + e.getMessage());
                });
    }
    
    /**
     * Register new user
     */
    public static void registerUser(Context context, String username, String password, String email, RegisterCallback callback) {
        if (username == null || username.trim().isEmpty()) {
            callback.onError("Username cannot be empty");
            return;
        }
        
        if (password == null || password.length() < 6) {
            callback.onError("Password must be at least 6 characters");
            return;
        }
        
        if (email == null || !email.contains("@")) {
            callback.onError("Please enter a valid email address");
            return;
        }
        
        // Check if username already exists
        db.collection("users")
                .whereEqualTo("name", username.trim())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            callback.onError("Username already exists");
                            return;
                        }
                        
                        // Create new user
                        UserModel newUser = new UserModel();
                        newUser.setName(username.trim());
                        newUser.setPassword(password); // Note: In production, hash the password
                        newUser.setRole(Arrays.asList("REGISTERED_USER"));
                        newUser.setLocation("Not specified");
                        
                        // Add to Firestore
                        db.collection("users")
                                .add(newUser)
                                .addOnSuccessListener(documentReference -> {
                                    newUser.setUserId(documentReference.getId());
                                    saveUserSession(context, newUser);
                                    callback.onSuccess(newUser);
                                    Log.d(TAG, "User registered successfully: " + username);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Registration error", e);
                                    callback.onError("Registration failed: " + e.getMessage());
                                });
                    } else {
                        callback.onError("Failed to check username availability");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Username check error", e);
                    callback.onError("Registration failed: " + e.getMessage());
                });
    }    /**
     * Save user session to SharedPreferences
     */
    public static void saveUserSession(Context context, UserModel user) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getName());
        editor.putString(KEY_USER_ROLE, user.getRole() != null && !user.getRole().isEmpty() 
                ? user.getRole().get(0) : "REGISTERED_USER");
        editor.putBoolean(KEY_IS_TUTOR, user.getRole() != null && user.getRole().contains("TUTOR"));
        
        editor.apply();
    }
    
    /**
     * Sync user session with Firestore to ensure data consistency across devices
     */
    public static void syncUserSession(Context context, AuthCallback callback) {
        String userId = getCurrentUserId(context);
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("No user session found");
            return;
        }
        
        Log.d(TAG, "Syncing user session with Firestore for user ID: " + userId);
        
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            user.setUserId(documentSnapshot.getId());
                            saveUserSession(context, user);
                            Log.d(TAG, "User session synced successfully");
                            if (callback != null) callback.onSuccess(user);
                        } else {
                            Log.e(TAG, "Failed to parse user document during sync");
                            if (callback != null) callback.onError("Failed to parse user data");
                        }
                    } else {
                        Log.w(TAG, "User document not found during sync, signing out");
                        signOut(context);
                        if (callback != null) callback.onError("User account not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to sync user session with Firestore", e);
                    if (callback != null) callback.onError("Sync failed: " + e.getMessage());
                });
    }
    
    /**
     * Get current user info
     */
    public static UserModel getCurrentUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return null;
        }
        
        UserModel user = new UserModel();
        user.setUserId(prefs.getString(KEY_USER_ID, ""));
        user.setName(prefs.getString(KEY_USERNAME, ""));
        user.setRole(Arrays.asList(prefs.getString(KEY_USER_ROLE, "REGISTERED_USER")));
        
        return user;
    }
    
    /**
     * Check if user is logged in
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        String userId = prefs.getString(KEY_USER_ID, null);
        
        Log.d(TAG, "isLoggedIn() check - Logged in: " + isLoggedIn + ", User ID: " + userId);
        
        return isLoggedIn && userId != null && !userId.isEmpty();
    }
    
    /**
     * Get current user ID with enhanced logging
     */
    public static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString(KEY_USER_ID, null);
        Log.d(TAG, "getCurrentUserId() returning: " + userId);
        return userId;
    }
    
    /**
     * Get current username with enhanced logging
     */
    public static String getCurrentUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, null);
        Log.d(TAG, "getCurrentUsername() returning: " + username);
        return username;
    }
    
    /**
     * Get current user email
     */
    public static String getCurrentUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, "");
    }
    
    /**
     * Check if current user is a tutor
     */
    public static boolean isCurrentUserTutor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_TUTOR, false);
    }
    
    /**
     * Get user role
     */
    public static UserRole getCurrentUserRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String role = prefs.getString(KEY_USER_ROLE, "GUEST");
        
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            return UserRole.GUEST;
        }
    }
    
    /**
     * Apply to become a tutor role
     */
    public static void applyForTutorRole(Context context, String experience, String education, String subjects, AuthCallback callback) {
        String userId = getCurrentUserId(context);
        if (userId.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }
        
        // For demo purposes, auto-approve tutor applications
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            // Add TUTOR role
                            if (user.getRole() == null) {
                                user.setRole(Arrays.asList("REGISTERED_USER", "TUTOR"));
                            } else if (!user.getRole().contains("TUTOR")) {
                                user.getRole().add("TUTOR");
                            }
                            
                            // Update in Firestore
                            db.collection("users")
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        saveUserSession(context, user);
                                        callback.onSuccess(user);
                                        Log.d(TAG, "User approved as tutor: " + user.getName());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to update user role", e);
                                        callback.onError("Failed to update user role");
                                    });
                        } else {
                            callback.onError("Failed to get user data");
                        }
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user data", e);
                    callback.onError("Failed to get user data");
                });
    }
    
    /**
     * Promote user to tutor role (admin function)
     */
    public static void promoteToTutor(Context context, String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "Cannot promote user: userId is empty");
            return;
        }
        
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            // Add TUTOR role if not already present
                            if (user.getRole() == null) {
                                user.setRole(Arrays.asList("REGISTERED_USER", "TUTOR"));
                            } else if (!user.getRole().contains("TUTOR")) {
                                user.getRole().add("TUTOR");
                            }
                            
                            // Update in Firestore
                            db.collection("users")
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User promoted to tutor: " + user.getName());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to promote user to tutor", e);
                                    });
                        }
                    } else {
                        Log.w(TAG, "User not found for promotion: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user data for promotion", e);
                });
    }
    
    /**
     * Sign out user
     */
    public static void signOut(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "User signed out");
    }
    
    /**
     * Check if user has specific role
     */
    public static boolean hasRole(Context context, String role) {
        UserModel user = getCurrentUser(context);
        return user != null && user.getRole() != null && user.getRole().contains(role);
    }    /**
     * Handle demo user logins for testing purposes - now supports demo tutors from DummyDataGenerator
     */
    private static boolean handleDemoUsers(Context context, String username, String password, AuthCallback callback) {
        // Check if this is a demo user login attempt
        UserRole demoRole = null;
        
        // Original demo users
        if ("demo".equals(username) && "demo".equals(password)) {
            demoRole = UserRole.REGISTERED_USER;
        } else if ("admin".equals(username) && "admin".equals(password)) {
            demoRole = UserRole.ADMIN;
        } else if ("tutor".equals(username) && "tutor".equals(password)) {
            demoRole = UserRole.TUTOR;
        } else if ("developer".equals(username) && "developer".equals(password)) {
            demoRole = UserRole.ADMIN;
        }
        // Demo tutors from DummyDataGenerator - check for common demo tutor password
        else if ("tutor123".equals(password) && isDemoTutorUsername(username)) {
            demoRole = UserRole.TUTOR;
        }
        // Demo students from DummyDataGenerator - check for common demo student password
        else if ("student123".equals(password) && isDemoStudentUsername(username)) {
            demoRole = UserRole.REGISTERED_USER;
        }
        
        if (demoRole != null) {
            // This is a demo user, but we still check/create them in Firestore
            final UserRole finalRole = demoRole;
            db.collection("users")
                    .whereEqualTo("name", username)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Demo user exists in Firestore
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            UserModel existingUser = document.toObject(UserModel.class);
                            if (existingUser != null) {
                                existingUser.setUserId(document.getId());
                                saveUserSession(context, existingUser);
                                callback.onSuccess(existingUser);
                                Log.d(TAG, "Existing demo user signed in from Firestore: " + existingUser.getName());
                            }
                        } else {
                            // Create demo user in Firestore
                            UserModel demoUser = new UserModel();
                            demoUser.setName(username);
                            demoUser.setPassword(password);
                            demoUser.setLocation("Demo Location");
                            
                            switch (finalRole) {
                                case ADMIN:
                                    demoUser.setRole(Arrays.asList("ADMIN", "REGISTERED_USER"));
                                    break;
                                case TUTOR:
                                    demoUser.setRole(Arrays.asList("TUTOR", "REGISTERED_USER"));
                                    break;
                                case REGISTERED_USER:
                                default:
                                    demoUser.setRole(Arrays.asList("REGISTERED_USER"));
                                    break;
                            }
                            
                            db.collection("users")
                                    .add(demoUser)
                                    .addOnSuccessListener(documentReference -> {
                                        demoUser.setUserId(documentReference.getId());
                                        saveUserSession(context, demoUser);
                                        callback.onSuccess(demoUser);
                                        Log.d(TAG, "New demo user created in Firestore: " + demoUser.getName());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to create demo user in Firestore", e);
                                        callback.onError("Failed to create demo user: " + e.getMessage());
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to check for demo user in Firestore", e);
                        callback.onError("Login failed: " + e.getMessage());
                    });
            return true;
        }
        
        return false;
    }    /**
     * Simplified sign in method (convenience method for legacy code)
     */
    public static boolean signIn(Context context, String username, String password) {
        // For immediate authentication in simple cases, we'll use a simplified approach
        if (username == null || password == null) {
            return false;
        }
        
        // Check for special demo/admin users
        if (username.equals("demo") && password.equals("demo123")) {
            signInDemoUser(context, "demo", UserRole.REGISTERED_USER);
            return true;
        } else if (username.equals("developer") && password.equals("dev123")) {
            signInDemoUser(context, "developer", UserRole.ADMIN);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if username matches a demo tutor from DummyDataGenerator
     */
    private static boolean isDemoTutorUsername(String username) {
        String[] demoTutorNames = {
            "María González", "Jean-Pierre Dubois", "Hiroshi Tanaka", "Dr. Sarah Johnson",
            "Prof. Michael Chen", "Dr. Emma Watson", "Isabella Rodriguez", "David Thompson",
            "Sofia Andersson", "James Miller"
        };
        
        for (String name : demoTutorNames) {
            if (name.equals(username)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if username matches a demo student from DummyDataGenerator
     */
    private static boolean isDemoStudentUsername(String username) {
        String[] demoStudentNames = {
            "Alex Johnson", "Emily Davis", "Carlos Rodriguez", 
            "Lisa Zhang", "Ahmed Hassan", "Maria Silva"
        };
        
        for (String name : demoStudentNames) {
            if (name.equals(username)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Initialize demo users if they don't exist - integrates with DummyDataGenerator
     */
    public static void initializeDemoUsers() {
        Log.d(TAG, "Checking if demo users need to be initialized");
        
        // Check if demo tutors exist
        db.collection("users")
                .whereArrayContains("role", "tutor")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No demo tutors found, initializing demo users");
                        DummyDataGenerator.generateAllDummyUsers();
                        Log.d(TAG, "Demo users initialized");
                    } else {
                        Log.d(TAG, "Demo users already exist, skipping initialization");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check for existing demo users", e);
                });
    }
    
    /**
     * Get list of demo tutor credentials for testing
     */
    public static String[][] getDemoTutorCredentials() {
        return new String[][]{
            {"María González", "tutor123"},
            {"Jean-Pierre Dubois", "tutor123"},
            {"Hiroshi Tanaka", "tutor123"},
            {"Dr. Sarah Johnson", "tutor123"},
            {"Prof. Michael Chen", "tutor123"},
            {"Dr. Emma Watson", "tutor123"},
            {"Isabella Rodriguez", "tutor123"},
            {"David Thompson", "tutor123"},
            {"Sofia Andersson", "tutor123"},
            {"James Miller", "tutor123"}
        };
    }
    
    /**
     * Get list of demo student credentials for testing
     */
    public static String[][] getDemoStudentCredentials() {
        return new String[][]{
            {"Alex Johnson", "student123"},
            {"Emily Davis", "student123"},
            {"Carlos Rodriguez", "student123"},
            {"Lisa Zhang", "student123"},
            {"Ahmed Hassan", "student123"},
            {"Maria Silva", "student123"}
        };
    }
}
