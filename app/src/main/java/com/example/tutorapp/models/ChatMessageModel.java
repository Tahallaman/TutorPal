package com.example.tutorapp.models;

import java.util.Date;

/**
 * Represents a chat message in Firestore (subcollection of Chat).
 */
public class ChatMessageModel {
    private String messageID;
    private String senderID;
    private String text;
    private Date timestamp;
    private boolean seen;
    private String messageType; // Added for message type (text, image, etc.)
    private String senderName; // Added for sender name display

    public ChatMessageModel() {}

    public ChatMessageModel(String messageID, String senderID, String text, Date timestamp, boolean seen) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.text = text;
        this.timestamp = timestamp;
        this.seen = seen;
        this.messageType = "text"; // Default to text
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }    // Adapter compatibility methods
    public String getMessage() {
        return text;
    }

    public boolean isRead() {
        return seen;
    }

    public String getMessageType() {
        return messageType != null ? messageType : "text";
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    // Additional setter methods for compatibility
    public String getId() {
        return messageID;
    }

    public void setId(String id) {
        this.messageID = id;
    }

    public void setChatId(String chatId) {
        // For compatibility - could be used for parent chat reference
    }

    public void setMessage(String message) {
        this.text = message;
    }

    public void setRead(boolean read) {
        this.seen = read;
    }
}

