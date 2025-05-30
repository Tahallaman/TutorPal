package com.example.tutorapp.utils;

import com.example.tutorapp.models.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages tutor user operations including converting users to tutors,
 * retrieving tutor users, and managing tutor-specific functionality.
 */
public class TutorUserManager {
    
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    public interface TutorCallback {
        void onSuccess(UserModel user);
        void onError(String error);
    }
    
    public interface TutorListCallback {
        void onSuccess(List<UserModel> tutors);
        void onError(String error);
    }
    
    /**
     * Convert an existing user to also be a tutor by adding tutor role and fields
     */
    public static void promoteUserToTutor(String userId, UserModel tutorData, TutorCallback callback) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    if (user != null) {
                        // Add tutor role if not already present
                        List<String> roles = user.getRole();
                        if (roles == null) {
                            roles = new ArrayList<>();
                        }
                        if (!roles.contains("tutor")) {
                            roles.add("tutor");
                            user.setRole(roles);
                        }
                        
                        // Copy tutor-specific data
                        user.setSubject(tutorData.getSubject());
                        user.setBio(tutorData.getBio());
                        user.setDescription(tutorData.getDescription());
                        user.setExperience(tutorData.getExperience());
                        user.setPricePerHour(tutorData.getPricePerHour());
                        user.setAvailability(tutorData.getAvailability());
                        user.setCategories(tutorData.getCategories());
                        user.setImageUrl(tutorData.getImageUrl());
                        user.setEmail(tutorData.getEmail());
                        user.setPhone(tutorData.getPhone());
                        user.setEducation(tutorData.getEducation());
                        
                        // Set default values for new tutors
                        user.setRatingAverage(0.0);
                        user.setReviewCount(0);
                        user.setTotalStudents(0);
                        user.setVerified(false);
                        
                        // Save updated user
                        db.collection("users").document(userId).set(user)
                            .addOnSuccessListener(aVoid -> {
                                // Also add to tutors collection for backward compatibility
                                addToTutorsCollection(user);
                                callback.onSuccess(user);
                            })
                            .addOnFailureListener(e -> callback.onError("Failed to update user: " + e.getMessage()));
                    } else {
                        callback.onError("Failed to parse user data");
                    }
                } else {
                    callback.onError("User not found");
                }
            })
            .addOnFailureListener(e -> callback.onError("Failed to fetch user: " + e.getMessage()));
    }
    
    /**
     * Get all users who are tutors
     */
    public static void getAllTutors(TutorListCallback callback) {
        db.collection("users")
            .whereArrayContains("role", "tutor")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<UserModel> tutors = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    UserModel user = document.toObject(UserModel.class);
                    if (user != null) {
                        tutors.add(user);
                    }
                }
                callback.onSuccess(tutors);
            })
            .addOnFailureListener(e -> callback.onError("Failed to fetch tutors: " + e.getMessage()));
    }
    
    /**
     * Get tutors by category
     */
    public static void getTutorsByCategory(String category, TutorListCallback callback) {
        db.collection("users")
            .whereArrayContains("role", "tutor")
            .whereArrayContains("categories", category)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<UserModel> tutors = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    UserModel user = document.toObject(UserModel.class);
                    if (user != null) {
                        tutors.add(user);
                    }
                }
                callback.onSuccess(tutors);
            })
            .addOnFailureListener(e -> callback.onError("Failed to fetch tutors by category: " + e.getMessage()));
    }
    
    /**
     * Get a specific tutor by user ID
     */
    public static void getTutorById(String userId, TutorCallback callback) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    if (user != null && user.isTutor()) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("User is not a tutor");
                    }
                } else {
                    callback.onError("Tutor not found");
                }
            })
            .addOnFailureListener(e -> callback.onError("Failed to fetch tutor: " + e.getMessage()));
    }
    
    /**
     * Update tutor profile information
     */
    public static void updateTutorProfile(String userId, UserModel updatedData, TutorCallback callback) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    if (user != null && user.isTutor()) {
                        // Update tutor-specific fields
                        user.setSubject(updatedData.getSubject());
                        user.setBio(updatedData.getBio());
                        user.setDescription(updatedData.getDescription());
                        user.setExperience(updatedData.getExperience());
                        user.setPricePerHour(updatedData.getPricePerHour());
                        user.setAvailability(updatedData.getAvailability());
                        user.setCategories(updatedData.getCategories());
                        user.setImageUrl(updatedData.getImageUrl());
                        user.setEmail(updatedData.getEmail());
                        user.setPhone(updatedData.getPhone());
                        user.setEducation(updatedData.getEducation());
                        
                        // Save updated user
                        db.collection("users").document(userId).set(user)
                            .addOnSuccessListener(aVoid -> {
                                // Update tutors collection for backward compatibility
                                updateTutorsCollection(user);
                                callback.onSuccess(user);
                            })
                            .addOnFailureListener(e -> callback.onError("Failed to update tutor: " + e.getMessage()));
                    } else {
                        callback.onError("User is not a tutor");
                    }
                } else {
                    callback.onError("Tutor not found");
                }
            })
            .addOnFailureListener(e -> callback.onError("Failed to fetch tutor: " + e.getMessage()));
    }
    
    /**
     * Add user to tutors collection for backward compatibility with existing tutor-related code
     */
    private static void addToTutorsCollection(UserModel user) {
        db.collection("tutors").document(user.getUserId()).set(user);
    }
    
    /**
     * Update tutors collection for backward compatibility
     */
    private static void updateTutorsCollection(UserModel user) {
        db.collection("tutors").document(user.getUserId()).set(user);
    }
    
    /**
     * Check if a user can become a tutor (basic validation)
     */
    public static boolean canBecomeTeacher(UserModel user) {
        return user != null && 
               user.getName() != null && !user.getName().trim().isEmpty() &&
               user.getLocation() != null && !user.getLocation().trim().isEmpty();
    }
}
