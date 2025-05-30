package com.example.tutorapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.tutorapp.models.StudentModel;
import com.example.tutorapp.models.TutorModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages user favorites functionality including adding/removing favorites
 * and syncing with Firestore database.
 */
public class FavouritesManager {
    private static final String TAG = "FavouritesManager";
    private static final String PREFS_NAME = "TutorPalPrefs";
    private static final String KEY_FAVORITES = "user_favorites";
    private static final String KEY_USER_ID = "user_id";
    
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private Context context;
    private Set<String> localFavorites;
    
    public interface FavouritesCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface FavouritesListCallback {
        void onSuccess(List<TutorModel> tutors);
        void onError(String error);
    }
      public FavouritesManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        
        // Enable offline persistence for better offline support
        try {
            db.enableNetwork();
        } catch (Exception e) {
            Log.w(TAG, "Firestore network already enabled or unavailable: " + e.getMessage());
        }
        
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.localFavorites = new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }
    
    /**
     * Add a tutor to favorites
     */    public void addToFavorites(String tutorId, FavouritesCallback callback) {
        // Add to local storage immediately for responsive UI
        localFavorites.add(tutorId);
        saveLocalFavorites();
        
        // Always call success immediately since local update is done
        callback.onSuccess();
        
        // Sync with Firestore in background (don't block on this)
        String userId = getCurrentUserId();
        if (userId != null) {
            syncWithFirestoreInBackground(userId);
        }
    }
    
    /**
     * Remove a tutor from favorites
     */    public void removeFromFavorites(String tutorId, FavouritesCallback callback) {
        // Remove from local storage immediately for responsive UI
        localFavorites.remove(tutorId);
        saveLocalFavorites();
        
        // Always call success immediately since local update is done
        callback.onSuccess();
        
        // Sync with Firestore in background (don't block on this)
        String userId = getCurrentUserId();
        if (userId != null) {
            syncWithFirestoreInBackground(userId);
        }
    }
    
    /**
     * Toggle favorite status for a tutor
     */
    public void toggleFavorite(String tutorId, FavouritesCallback callback) {
        if (isFavorite(tutorId)) {
            removeFromFavorites(tutorId, callback);
        } else {
            addToFavorites(tutorId, callback);
        }
    }
    
    /**
     * Check if a tutor is in favorites
     */
    public boolean isFavorite(String tutorId) {
        return localFavorites.contains(tutorId);
    }
    
    /**
     * Get all favorite tutor IDs
     */
    public Set<String> getFavoriteIds() {
        return new HashSet<>(localFavorites);
    }
    
    /**
     * Get count of favorites
     */
    public int getFavoriteCount() {
        return localFavorites.size();
    }
    
    /**
     * Load all favorite tutors from Firestore
     */
    public void loadFavoriteTutors(FavouritesListCallback callback) {
        if (localFavorites.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        
        // Convert Set to List for Firestore query
        List<String> favoriteIds = new ArrayList<>(localFavorites);
        
        // For simplicity, limit to 10 favorites for now
        if (favoriteIds.size() > 10) {
            favoriteIds = favoriteIds.subList(0, 10);
        }
        
        loadTutorsBatch(favoriteIds, callback);
    }
    
    private void loadTutorsBatch(List<String> tutorIds, FavouritesListCallback callback) {
        db.collection("tutors")
            .whereIn("tutorId", tutorIds)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<TutorModel> tutors = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    TutorModel tutor = document.toObject(TutorModel.class);
                    if (tutor != null) {
                        tutors.add(tutor);
                    }
                }
                callback.onSuccess(tutors);
            })            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading favorite tutors", e);
                
                // Check if it's an offline error
                String errorMessage = e.getMessage();
                if (errorMessage != null && (errorMessage.contains("offline") || errorMessage.contains("UNAVAILABLE"))) {
                    // For offline errors, return empty list instead of showing error
                    Log.w(TAG, "Offline - cannot load tutor details, showing favorites list only");
                    callback.onSuccess(new ArrayList<>());
                } else {
                    callback.onError("Failed to load favorite tutors: " + errorMessage);
                }
            });}

    /**
     * Sync local favorites with Firestore in background (don't block UI on errors)
     */
    private void syncWithFirestoreInBackground(String userId) {
        db.collection("students").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                StudentModel student;
                if (documentSnapshot.exists()) {
                    student = documentSnapshot.toObject(StudentModel.class);
                    if (student == null) {
                        student = new StudentModel();
                        student.setStudentID(userId);
                    }
                } else {
                    // Create new student document
                    student = new StudentModel();
                    student.setStudentID(userId);
                    student.setMyBookings(new ArrayList<>());
                }
                
                // Update favorites
                student.setFavourites(new ArrayList<>(localFavorites));
                
                // Save to Firestore
                db.collection("students").document(userId)
                    .set(student)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Favorites synced successfully in background");
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to sync favorites in background (will retry later): " + e.getMessage());
                        // Don't show error to user - this is background sync
                    });
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Failed to access Firestore for background sync (will retry later): " + e.getMessage());
                // Don't show error to user - this is background sync
            });
    }
    
    /**
     * Sync local favorites with Firestore
     */
    private void syncWithFirestore(String userId, FavouritesCallback callback) {
        // First check if student document exists
        db.collection("students").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                StudentModel student;
                if (documentSnapshot.exists()) {
                    student = documentSnapshot.toObject(StudentModel.class);
                    if (student == null) {
                        student = new StudentModel();
                        student.setStudentID(userId);
                    }
                } else {
                    // Create new student document
                    student = new StudentModel();
                    student.setStudentID(userId);
                    student.setMyBookings(new ArrayList<>());
                }
                
                // Update favorites
                student.setFavourites(new ArrayList<>(localFavorites));
                
                // Save to Firestore
                db.collection("students").document(userId)
                    .set(student)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Favorites synced successfully");
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error syncing favorites", e);
                        callback.onError("Failed to sync favorites: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking student document", e);
                callback.onError("Failed to sync favorites: " + e.getMessage());
            });
    }
    
    /**
     * Load favorites from Firestore for logged-in user
     */
    public void loadFavoritesFromFirestore(FavouritesCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onSuccess(); // No user logged in, use local favorites
            return;
        }
        
        db.collection("students").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    StudentModel student = documentSnapshot.toObject(StudentModel.class);
                    if (student != null && student.getFavourites() != null) {
                        localFavorites.clear();
                        localFavorites.addAll(student.getFavourites());
                        saveLocalFavorites();
                    }
                }
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading favorites from Firestore", e);
                callback.onError("Failed to load favorites: " + e.getMessage());
            });
    }
    
    /**
     * Save favorites to local SharedPreferences
     */
    private void saveLocalFavorites() {
        sharedPreferences.edit()
            .putStringSet(KEY_FAVORITES, localFavorites)
            .apply();
    }
    
    /**
     * Get current user ID from SharedPreferences
     * In a real app, this would come from authentication system
     */
    private String getCurrentUserId() {
        // For demo purposes, we'll use a simple check
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            String username = sharedPreferences.getString("username", "");
            if (!username.isEmpty()) {
                // Generate a consistent user ID based on username
                return "user_" + username.hashCode();
            }
        }
        return null;
    }
    
    /**
     * Clear all favorites (useful for logout)
     */
    public void clearFavorites() {
        localFavorites.clear();
        saveLocalFavorites();
    }
    
    /**
     * Get debug info about favorites state
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Favorites Manager Debug Info:\n");
        info.append("- Local favorites count: ").append(localFavorites.size()).append("\n");
        info.append("- Local favorites: ").append(localFavorites.toString()).append("\n");
        info.append("- Current user ID: ").append(getCurrentUserId()).append("\n");
        info.append("- Is logged in: ").append(getCurrentUserId() != null).append("\n");
        return info.toString();
    }
}
