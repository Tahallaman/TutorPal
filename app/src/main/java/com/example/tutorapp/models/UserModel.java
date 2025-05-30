package com.example.tutorapp.models;

import java.util.List;
import java.util.Map;

/**
 * Represents a user in the TutorApp application.
 * Unified model that can represent students, tutors, admins, or combinations of roles.
 * When a user has the "tutor" role, tutor-specific fields are populated.
 */
public class UserModel {
    // Core user fields
    private String userId;
    private String name;
    private String password;
    private List<String> role; // e.g., ["student"], ["tutor"], ["student", "tutor"], ["admin"]
    private String location;
    
    // Tutor-specific fields (only populated when user has "tutor" role)
    private String subject;
    private String bio;
    private String description;
    private String experience;
    private double pricePerHour;
    private double ratingAverage;
    private int reviewCount;
    private int totalStudents;
    private Map<String, String> availability;
    private List<String> categories;
    private String imageUrl;
    private String email;
    private String phone;
    private String education;
    private boolean isVerified;
    
    // Student-specific fields (could be extended in the future)
    private List<String> favourites; // tutorIDs
    private List<String> myBookings; // bookingIDs

    public UserModel() {
    }

    // Constructor with core user fields
    public UserModel(String userId, String name, String password, List<String> role, String location) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.role = role;
        this.location = location;
    }

    // Core user getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<String> getRole() { return role; }
    public void setRole(List<String> role) { this.role = role; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    // Tutor-specific getters and setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    public double getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(double ratingAverage) { this.ratingAverage = ratingAverage; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public Map<String, String> getAvailability() { return availability; }
    public void setAvailability(Map<String, String> availability) { this.availability = availability; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    
    // Student-specific getters and setters
    public List<String> getFavourites() { return favourites; }
    public void setFavourites(List<String> favourites) { this.favourites = favourites; }

    public List<String> getMyBookings() { return myBookings; }
    public void setMyBookings(List<String> myBookings) { this.myBookings = myBookings; }
    
    // Helper methods
    public boolean isTutor() {
        return role != null && role.contains("tutor");
    }
    
    public boolean isStudent() {
        return role != null && (role.contains("student") || role.isEmpty());
    }
    
    public boolean isAdmin() {
        return role != null && role.contains("admin");
    }
    
    // Compatibility methods for TutorModel interface
    public String getTutorId() { return userId; }
    public void setTutorId(String tutorId) { this.userId = tutorId; }
    
    public String getId() { return userId; }
    public void setId(String id) { this.userId = id; }
    
    // Price compatibility methods
    public double getPrice() { return pricePerHour; }
    public void setPrice(double price) { this.pricePerHour = price; }
    
    public double getHourlyRate() { return pricePerHour; }
    public void setHourlyRate(double hourlyRate) { this.pricePerHour = hourlyRate; }
    
    // Rating compatibility methods
    public double getRating() { return ratingAverage; }
    public void setRating(double rating) { this.ratingAverage = rating; }
}
