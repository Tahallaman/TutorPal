package com.example.tutorapp.models;

import java.util.List;
import java.util.Map;

/**
 * Represents a tutor in the TutorApp application.
 * The class stores comprehensive information about a tutor including their profile,
 * pricing, availability, and contact details.
 */
public class TutorModel {
    private String tutorId;
    private String id; // Alternative ID field used in some contexts
    private String name;
    private String subject;
    private String bio;
    private String description; // Alternative description field
    private String location;
    private String experience;
    private double price; // Price field for compatibility
    private double hourlyRate; // Alternative price field
    private double pricePerHour; // Another alternative price field
    private double rating; // Rating field for compatibility
    private double ratingAverage; // Alternative rating field
    private int reviewCount;
    private int totalStudents;
    private Map<String, String> availability;
    private List<String> categories;
    private String imageUrl;
    private List<String> imageUrls; // Multiple image URLs for image slider
    private String email;
    private String phone;
    private String education;
    private boolean isVerified;

    // Default constructor required for Firestore
    public TutorModel() {
    }

    // Constructor with essential fields
    public TutorModel(String name, String subject, String bio, String location, double price) {
        this.name = name;
        this.subject = subject;
        this.bio = bio;
        this.location = location;
        this.price = price;
        this.hourlyRate = price;
        this.pricePerHour = price;
    }

    // Getters and setters for all fields
    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public double getPrice() { return price; }
    public void setPrice(double price) { 
        this.price = price;
        this.hourlyRate = price;
        this.pricePerHour = price;
    }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { 
        this.hourlyRate = hourlyRate;
        this.price = hourlyRate;
        this.pricePerHour = hourlyRate;
    }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { 
        this.pricePerHour = pricePerHour;
        this.price = pricePerHour;
        this.hourlyRate = pricePerHour;
    }

    public double getRating() { return rating; }
    public void setRating(double rating) { 
        this.rating = rating;
        this.ratingAverage = rating;
    }    public double getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(double ratingAverage) { 
        this.ratingAverage = ratingAverage;
        this.rating = ratingAverage;
    }
    
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

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    @Override
    public String toString() {
        return "TutorModel{" +
                "tutorId='" + tutorId + '\'' +
                ", name='" + name + '\'' +
                ", subject='" + subject + '\'' +
                ", location='" + location + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                '}';
    }
}