package com.example.tutorapp.models;

import java.util.List;

/**
 * Represents a student profile in Firestore.
 * Linked 1:1 with a UserModel via studentID/userId.
 */
public class StudentModel {
    private String studentID; // document ID, same as userID
    private List<String> favourites; // tutorIDs
    private List<String> myBookings; // bookingIDs

    public StudentModel() {}

    public StudentModel(String studentID, List<String> favourites, List<String> myBookings) {
        this.studentID = studentID;
        this.favourites = favourites;
        this.myBookings = myBookings;
    }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public List<String> getFavourites() { return favourites; }
    public void setFavourites(List<String> favourites) { this.favourites = favourites; }

    public List<String> getMyBookings() { return myBookings; }
    public void setMyBookings(List<String> myBookings) { this.myBookings = myBookings; }
}

