package com.example.tutorapp.models;

import java.util.Date;
import java.util.List;

/**
 * Model representing a conversation between a user and a tutor
 */
public class ConversationModel {
    private String id;
    private List<String> participants;
    private String lastMessage;
    private Date lastMessageTime;
    private String otherParticipantId;
    private String otherParticipantName;
    private String otherParticipantImageUrl;
    private boolean hasUnreadMessages;
    private int unreadCount;
    
    public ConversationModel() {
        // Default constructor required for Firestore
    }
    
    public ConversationModel(List<String> participants, String lastMessage, Date lastMessageTime) {
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public List<String> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public Date getLastMessageTime() {
        return lastMessageTime;
    }
    
    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    
    public String getOtherParticipantId() {
        return otherParticipantId;
    }
    
    public void setOtherParticipantId(String otherParticipantId) {
        this.otherParticipantId = otherParticipantId;
    }
    
    public String getOtherParticipantName() {
        return otherParticipantName;
    }
    
    public void setOtherParticipantName(String otherParticipantName) {
        this.otherParticipantName = otherParticipantName;
    }
    
    public String getOtherParticipantImageUrl() {
        return otherParticipantImageUrl;
    }
    
    public void setOtherParticipantImageUrl(String otherParticipantImageUrl) {
        this.otherParticipantImageUrl = otherParticipantImageUrl;
    }
    
    public boolean isHasUnreadMessages() {
        return hasUnreadMessages;
    }
    
    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }
    
    public int getUnreadCount() {
        return unreadCount;
    }
    
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
