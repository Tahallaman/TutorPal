package com.example.tutorapp.models;

import java.util.Date;

/**
 * Represents a review in Firestore.
 */
public class ReviewModel {
    private String reviewID;
    private String tutorID;
    private String studentID;
    private String studentName;
    private double rating;
    private String comment;
    private Date createdAt;
    private int helpfulCount; // Added for helpful count feature

    public ReviewModel() {}

    public ReviewModel(String reviewID, String tutorID, String studentID, String studentName, double rating, String comment, Date createdAt) {
        this.reviewID = reviewID;
        this.tutorID = tutorID;
        this.studentID = studentID;
        this.studentName = studentName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.helpfulCount = 0; // Default to 0
    }

    public String getReviewID() {
        return reviewID;
    }

    public void setReviewID(String reviewID) {
        this.reviewID = reviewID;
    }

    public String getTutorID() {
        return tutorID;
    }

    public void setTutorID(String tutorID) {
        this.tutorID = tutorID;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    // Additional setter methods for compatibility
    public void setTimestamp(Date timestamp) {
        this.createdAt = timestamp;
    }
}

