package com.example.tutorapp.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents a chat session in Firestore.
 */
public class ChatModel {
    private String chatID;
    private List<String> participants;
    private Map<String, Object> participantInfo; // e.g., { "userID1": {"name": "Alice", "unread": 0}, ... }
    private String lastMessageText;
    private Date lastMessageTimestamp;
    // Additional fields for chat functionality
    private String tutorId;
    private String studentId;
    private String tutorName;
    private String studentName;
    private int unreadCount;

    public ChatModel() {}

    public ChatModel(String chatID, List<String> participants, Map<String, Object> participantInfo, String lastMessageText, Date lastMessageTimestamp) {
        this.chatID = chatID;
        this.participants = participants;
        this.participantInfo = participantInfo;
        this.lastMessageText = lastMessageText;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Map<String, Object> getParticipantInfo() {
        return participantInfo;
    }

    public void setParticipantInfo(Map<String, Object> participantInfo) {
        this.participantInfo = participantInfo;
    }

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public Date getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Date lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // Additional getters and setters for chat functionality
    public String getId() {
        return chatID;
    }

    public void setId(String id) {
        this.chatID = id;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public String getLastMessage() {
        return lastMessageText;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessageText = lastMessage;
    }

    public Date getLastMessageTime() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTimestamp = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}

