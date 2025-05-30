package com.example.tutorapp.models;

import java.util.Date;

/**
 * Represents a booking in Firestore.
 */
public class BookingModel {
    private String bookingID;
    private String tutorID;
    private String studentID;
    private String tutorName;
    private String studentName;
    private Date startTime;
    private Date endTime;
    private String status;
    private String category;
    private String subject;
    private Date createdAt;
    // Additional fields for booking functionality
    private String sessionType; // online/in-person
    private int duration; // session duration in minutes
    private double price; // session price
    private Date sessionDate; // specific session date
    private String notes; // additional notes
    private String address; // address for in-person sessions

    public BookingModel() {}

    public BookingModel(String bookingID, String tutorID, String studentID, String tutorName, String studentName, Date startTime, Date endTime, String status, String category, String subject, Date createdAt, String sessionType, int duration, double price, Date sessionDate, String notes, String address) {
        this.bookingID = bookingID;
        this.tutorID = tutorID;
        this.studentID = studentID;
        this.tutorName = tutorName;
        this.studentName = studentName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.category = category;
        this.subject = subject;
        this.createdAt = createdAt;
        this.sessionType = sessionType;
        this.duration = duration;
        this.price = price;
        this.sessionDate = sessionDate;
        this.notes = notes;
        this.address = address;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
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

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }    // Additional getters and setters
    // Removed duplicate setTutorId to avoid Firebase conflict - use setTutorID instead

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getTimestamp() {
        return createdAt;
    }

    public void setTimestamp(Date timestamp) {
        this.createdAt = timestamp;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }    // Additional setter methods for compatibility removed to avoid Firebase conflict
    // Use setStudentID instead of setStudentId to match field naming convention

    // Additional getter methods for adapter compatibility
    public Date getBookingDate() {
        return this.sessionDate;
    }

    public String getLocation() {
        return this.address;
    }    public void setId(String id) {
        this.bookingID = id;
    }
    
    @Override
    public String toString() {
        return "BookingModel{" +
                "tutorID='" + tutorID + '\'' +
                ", studentID='" + studentID + '\'' +
                ", tutorName='" + tutorName + '\'' +
                ", studentName='" + studentName + '\'' +
                ", subject='" + subject + '\'' +
                ", sessionType='" + sessionType + '\'' +
                ", duration=" + duration +
                ", price=" + price +
                ", sessionDate=" + sessionDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}

