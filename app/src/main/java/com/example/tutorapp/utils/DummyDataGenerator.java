package com.example.tutorapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating dummy data for testing purposes
 */
public class DummyDataGenerator {
    
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    /**
     * Generate all demo users (both tutors and students)
     */
    public static void generateAllDummyUsers() {
        generateDummyTutors();
        generateDummyStudents();
    }
    
    /**
     * Generate demo tutor users (users with tutor role)
     */
    public static void generateDummyTutors() {
        List<Map<String, Object>> tutorUsers = createDummyTutorUsers();
        
        for (Map<String, Object> tutorUser : tutorUsers) {
            String userId = (String) tutorUser.get("userId");
            
            // Save to users collection as the primary storage
            db.collection("users")
                .document(userId)
                .set(tutorUser);
                
            // Also save to tutors collection for backward compatibility
            db.collection("tutors")
                .document(userId)
                .set(tutorUser);
        }
    }
    
    /**
     * Generate demo student users (users with student role only)
     */
    public static void generateDummyStudents() {
        List<Map<String, Object>> studentUsers = createDummyStudentUsers();
        
        for (Map<String, Object> studentUser : studentUsers) {
            String userId = (String) studentUser.get("userId");
            
            db.collection("users")
                .document(userId)
                .set(studentUser);
        }
    }
    
    /**
     * Generate demo categories for tutoring subjects
     */
    public static void generateDummyCategories() {
        Map<String, Object> categories = new HashMap<>();
        categories.put("Languages", Arrays.asList("Spanish", "French", "Japanese", "English", "German", "Italian"));
        categories.put("Academic", Arrays.asList("Mathematics", "Physics", "Chemistry", "Biology", "Computer Science"));
        categories.put("Music", Arrays.asList("Piano", "Guitar", "Violin", "Drums", "Singing"));
        categories.put("Arts", Arrays.asList("Digital Art", "Drawing", "Painting", "Photography", "UI/UX Design"));
        categories.put("Business", Arrays.asList("Finance", "Accounting", "Marketing", "Management", "Investment"));
        categories.put("Technology", Arrays.asList("Programming", "Web Development", "Data Science", "AI/ML"));
        
        db.collection("categories")
            .document("subject_categories")
            .set(categories);
    }

    /**
     * Generate demo reviews for tutors
     */
    public static void generateDummyReviews() {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        // Sample reviews for different tutors
        Map<String, Object> review1 = new HashMap<>();
        review1.put("reviewId", "review_001");
        review1.put("tutorId", "lang_001");
        review1.put("studentId", "student_001");
        review1.put("studentName", "Alex Johnson");
        review1.put("rating", 5);
        review1.put("comment", "Maria is an excellent Spanish teacher! Very patient and encouraging.");
        review1.put("timestamp", System.currentTimeMillis());
        reviews.add(review1);
        
        for (Map<String, Object> review : reviews) {
            String reviewId = (String) review.get("reviewId");
            db.collection("reviews")
                .document(reviewId)
                .set(review);
        }
    }
    
    // Helper method to create availability map from string
    private static Map<String, String> createAvailabilityMap(String availabilityText) {
        Map<String, String> availability = new HashMap<>();
        availability.put("schedule", availabilityText);
        return availability;
    }
    
    private static List<Map<String, Object>> createDummyTutorUsers() {
        List<Map<String, Object>> tutorUsers = new ArrayList<>();
        
        // Language Tutors
        Map<String, Object> tutor1 = new HashMap<>();
        tutor1.put("userId", "lang_001");
        tutor1.put("name", "Maria Gonz?lez");
        tutor1.put("password", "tutor123");
        tutor1.put("role", Arrays.asList("tutor"));
        tutor1.put("location", "Madrid, Spain");
        tutor1.put("subject", "Spanish Language");
        tutor1.put("bio", "Native Spanish speaker passionate about teaching");
        tutor1.put("description", "Native Spanish speaker with 8+ years of teaching experience.");
        tutor1.put("experience", "8 years");
        tutor1.put("ratingAverage", 4.9);
        tutor1.put("pricePerHour", 25.0);
        tutor1.put("availability", createAvailabilityMap("Monday-Friday: 9AM-6PM"));
        tutor1.put("categories", Arrays.asList("Languages", "Spanish"));
        tutor1.put("imageUrl", "https://example.com/maria.jpg");
        tutor1.put("email", "maria.gonzalez@tutorpal.com");
        tutor1.put("phone", "+34 123 456 789");
        tutor1.put("verified", true);
        tutor1.put("totalStudents", 156);
        tutorUsers.add(tutor1);
        
        // Academic Tutors
        Map<String, Object> tutor2 = new HashMap<>();
        tutor2.put("userId", "acad_001");
        tutor2.put("name", "Dr. Sarah Johnson");
        tutor2.put("password", "tutor123");
        tutor2.put("role", Arrays.asList("tutor"));
        tutor2.put("location", "Boston, MA, USA");
        tutor2.put("subject", "Mathematics");
        tutor2.put("bio", "PhD mathematician with 15+ years experience");
        tutor2.put("description", "PhD in Mathematics with 15+ years teaching experience.");
        tutor2.put("experience", "15 years");
        tutor2.put("ratingAverage", 4.9);
        tutor2.put("pricePerHour", 40.0);
        tutor2.put("availability", createAvailabilityMap("Monday-Thursday: 2PM-9PM"));
        tutor2.put("categories", Arrays.asList("Academic", "Mathematics"));
        tutor2.put("imageUrl", "https://example.com/sarah.jpg");
        tutor2.put("email", "sarah.johnson@tutorpal.com");
        tutor2.put("phone", "+1 (617) 555-0123");
        tutor2.put("verified", true);
        tutor2.put("totalStudents", 487);
        tutorUsers.add(tutor2);
        
        return tutorUsers;
    }
    
    private static List<Map<String, Object>> createDummyStudentUsers() {
        List<Map<String, Object>> studentUsers = new ArrayList<>();
        
        Map<String, Object> student1 = new HashMap<>();
        student1.put("userId", "student_001");
        student1.put("name", "Alex Johnson");
        student1.put("password", "student123");
        student1.put("role", Arrays.asList("student"));
        student1.put("location", "Los Angeles, CA, USA");
        student1.put("email", "alex.johnson@student.com");
        student1.put("phone", "+1 (323) 555-0111");
        student1.put("favourites", new ArrayList<>());
        student1.put("myBookings", new ArrayList<>());
        studentUsers.add(student1);
        
        return studentUsers;
    }
}
