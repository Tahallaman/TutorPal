package com.example.tutorapp.utils;

import android.util.Log;

import com.example.tutorapp.models.TutorModel;
import com.example.tutorapp.models.ReviewModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataSeeder {
    private static final String TAG = "DataSeeder";
    private FirebaseFirestore db;
    
    public DataSeeder() {
        db = FirebaseFirestore.getInstance();
    }
    
    public void seedSampleData() {
        Log.d(TAG, "Starting to seed sample data...");
        seedTutors();
        seedCategories();
    }
      private void seedTutors() {
        // Sample tutor data
        TutorModel[] tutors = {
            createTutor("Sarah Johnson", "Mathematics", "Experienced math tutor with 5+ years", 
                       "New York, NY", 45.0, 4.8, 23, "5+ years", 
                       Arrays.asList("English", "Spanish"), 
                       Arrays.asList("Weekdays", "Online"),
                       "sarah_johnson_001"),
                       
            createTutor("Michael Chen", "Computer Science", "Software engineer and coding instructor", 
                       "San Francisco, CA", 65.0, 4.9, 18, "7+ years", 
                       Arrays.asList("English", "Mandarin"), 
                       Arrays.asList("Evenings", "Weekends", "Online"),
                       "michael_chen_002"),
                       
            createTutor("Emily Rodriguez", "Spanish", "Native Spanish speaker and language teacher", 
                       "Miami, FL", 35.0, 4.7, 31, "3+ years", 
                       Arrays.asList("English", "Spanish"), 
                       Arrays.asList("Weekdays", "In-person"),
                       "emily_rodriguez_003"),
                       
            createTutor("David Kim", "Physics", "PhD in Physics with teaching passion", 
                       "Boston, MA", 55.0, 4.9, 12, "8+ years", 
                       Arrays.asList("English", "Korean"), 
                       Arrays.asList("Weekdays", "Weekends", "Online"),
                       "david_kim_004"),
                       
            createTutor("Jessica Brown", "English Literature", "Published author and writing coach", 
                       "Chicago, IL", 40.0, 4.6, 27, "4+ years", 
                       Arrays.asList("English"), 
                       Arrays.asList("Weekdays", "Online", "In-person"),
                       "jessica_brown_005")
        };
        
        for (TutorModel tutor : tutors) {
            db.collection("tutors")
                .document(tutor.getId())
                .set(tutor)
                .addOnSuccessListener(aVoid -> 
                    Log.d(TAG, "Tutor added: " + tutor.getName()))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error adding tutor: " + tutor.getName(), e));
        }
        
        // Add sample reviews for tutors
        seedReviews();
    }
      private TutorModel createTutor(String name, String subject, String bio, String location, 
                                  double price, double rating, int reviewCount, String experience,
                                  java.util.List<String> languages, java.util.List<String> availability,
                                  String id) {
        TutorModel tutor = new TutorModel();
        tutor.setId(id);
        tutor.setName(name);        tutor.setSubject(subject);
        tutor.setBio(bio);
        tutor.setLocation(location);
        tutor.setPrice(price);
        tutor.setRating(rating);
        tutor.setReviewCount(reviewCount);
        tutor.setExperience(experience);
        // Convert List<String> to Map<String,String> for availability
        java.util.Map<String, String> availabilityMap = new java.util.HashMap<>();
        for (String day : availability) {
            availabilityMap.put(day, "9AM-5PM"); // Default time slot
        }
        tutor.setAvailability(availabilityMap);
        tutor.setVerified(true);
        tutor.setImageUrl(""); // Empty for now, will use placeholder
        return tutor;
    }
    
    private void seedReviews() {
        // Sample reviews for Sarah Johnson
        ReviewModel[] sarahReviews = {
            createReview("sarah_johnson_001", "Alice Smith", 5.0, 
                        "Sarah is an amazing math tutor! She helped me understand calculus concepts that I struggled with for months.", 
                        "alice_smith_001"),
            createReview("sarah_johnson_001", "John Doe", 4.5, 
                        "Very patient and explains complex topics in simple terms. Highly recommended!", 
                        "john_doe_001"),
            createReview("sarah_johnson_001", "Maria Garcia", 5.0, 
                        "Thanks to Sarah, I improved my SAT math score by 150 points!", 
                        "maria_garcia_001")
        };
        
        // Sample reviews for Michael Chen
        ReviewModel[] michaelReviews = {
            createReview("michael_chen_002", "Robert Wilson", 5.0, 
                        "Michael helped me learn Python from scratch. Now I'm confident in my coding skills!", 
                        "robert_wilson_001"),
            createReview("michael_chen_002", "Lisa Chang", 4.8, 
                        "Great instructor with real-world experience. His projects are very practical.", 
                        "lisa_chang_001")
        };
        
        // Add all reviews to Firestore
        addReviewsToFirestore(sarahReviews);
        addReviewsToFirestore(michaelReviews);
    }
    
    private ReviewModel createReview(String tutorId, String studentName, double rating, 
                                   String comment, String studentId) {        ReviewModel review = new ReviewModel();
        review.setTutorID(tutorId);
        review.setStudentID(studentId);
        review.setStudentName(studentName);
        review.setRating(rating);
        review.setComment(comment);
        review.setTimestamp(new Date());
        review.setHelpfulCount((int) (Math.random() * 10)); // Random helpful count
        return review;
    }
    
    private void addReviewsToFirestore(ReviewModel[] reviews) {
        for (ReviewModel review : reviews) {
            db.collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> 
                    Log.d(TAG, "Review added for tutor: " + review.getTutorID()))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error adding review", e));
        }
    }
    
    private void seedCategories() {
        String[] categories = {
            "Mathematics", "Science", "Languages", "Computer Science", 
            "English", "History", "Art", "Music", "Test Prep", "Business"
        };
        
        for (String category : categories) {
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("name", category);
            categoryData.put("tutorCount", (int) (Math.random() * 50) + 10); // Random count 10-60
            categoryData.put("icon", "ic_category_" + category.toLowerCase().replace(" ", "_"));
            
            db.collection("categories")
                .document(category.toLowerCase().replace(" ", "_"))
                .set(categoryData)
                .addOnSuccessListener(aVoid -> 
                    Log.d(TAG, "Category added: " + category))
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error adding category: " + category, e));
        }
    }
}
