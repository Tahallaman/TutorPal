package com.example.tutorapp.models;

import java.util.Date;
import java.util.List;

/**
 * Represents a tutor application in Firestore.
 * This model stores information about users applying to become tutors.
 */
public class TutorApplicationModel {
    private String applicationId;
    private String applicantId;
    private String applicantName;
    private String email;
    private String phone;
    private String education;
    private String experience;
    private List<String> subjects;
    private List<String> categories;
    private String bio;
    private String qualifications;
    private String preferredLocation;
    private double requestedHourlyRate;
    private String applicationStatus; // "pending", "approved", "rejected"
    private Date applicationDate;
    private Date reviewDate;
    private String reviewedBy; // Admin user who reviewed the application
    private String reviewNotes;
    
    // Default constructor required for Firestore
    public TutorApplicationModel() {
        this.applicationDate = new Date();
        this.applicationStatus = "pending";
    }
    
    // Constructor with essential fields
    public TutorApplicationModel(String applicantId, String applicantName, String email, 
                               String phone, String education, String experience, 
                               List<String> subjects, List<String> categories, String bio) {
        this();
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.email = email;
        this.phone = phone;
        this.education = education;
        this.experience = experience;
        this.subjects = subjects;
        this.categories = categories;
        this.bio = bio;
    }
    
    // Getters and setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    
    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }
    
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }
    
    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }
    
    public double getRequestedHourlyRate() { return requestedHourlyRate; }
    public void setRequestedHourlyRate(double requestedHourlyRate) { this.requestedHourlyRate = requestedHourlyRate; }
    
    public String getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(String applicationStatus) { this.applicationStatus = applicationStatus; }
    
    public Date getApplicationDate() { return applicationDate; }
    public void setApplicationDate(Date applicationDate) { this.applicationDate = applicationDate; }
    
    public Date getReviewDate() { return reviewDate; }
    public void setReviewDate(Date reviewDate) { this.reviewDate = reviewDate; }
    
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    
    @Override
    public String toString() {
        return "TutorApplicationModel{" +
                "applicationId='" + applicationId + '\'' +
                ", applicantName='" + applicantName + '\'' +
                ", email='" + email + '\'' +
                ", applicationStatus='" + applicationStatus + '\'' +
                ", applicationDate=" + applicationDate +
                '}';
    }
}
