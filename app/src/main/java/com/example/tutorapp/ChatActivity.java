package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.adapters.MessageAdapter;
import com.example.tutorapp.models.ChatMessageModel;
import com.example.tutorapp.models.ChatModel;
import com.example.tutorapp.utils.AuthenticationManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    
    private FirebaseFirestore db;
    private ListenerRegistration messagesListener;
    
    // Intent data
    private String tutorId;
    private String tutorName;
    private String chatId;
    
    // UI Components
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton, attachmentButton;
    private FloatingActionButton videoCallButton;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    
    private MessageAdapter messageAdapter;
    private List<ChatMessageModel> messagesList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
      // Current user info from authentication
    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        Log.d(TAG, "ChatActivity onCreate started");
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Firestore initialized successfully");
        
        // Check authentication first
        if (!AuthenticationManager.isLoggedIn(this)) {
            Log.e(TAG, "User not logged in, redirecting to authentication");
            Toast.makeText(this, "Please sign in to access chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Get current user info from authentication
        currentUserId = AuthenticationManager.getCurrentUserId(this);
        currentUserName = AuthenticationManager.getCurrentUsername(this);
        
        Log.d(TAG, "Current user - ID: " + currentUserId + ", Name: " + currentUserName);
        
        // Get data from intent
        getIntentData();
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        initializeChat();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firestore listener
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
      private void getIntentData() {
        Intent intent = getIntent();
        tutorId = intent.getStringExtra("tutor_id");
        tutorName = intent.getStringExtra("tutor_name");
        
        Log.d(TAG, "Intent data - Tutor ID: " + tutorId + ", Tutor Name: " + tutorName);
        
        if (TextUtils.isEmpty(tutorId)) {
            Log.e(TAG, "Invalid tutor ID received from intent");
            Toast.makeText(this, "Error: Invalid tutor information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (TextUtils.isEmpty(currentUserId)) {
            Log.e(TAG, "Current user ID is null or empty");
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Generate chat ID based on user IDs (ensures same chat for both users)
        chatId = generateChatId(currentUserId, tutorId);
        Log.d(TAG, "Generated chat ID: " + chatId);
    }
    
    private String generateChatId(String userId1, String userId2) {
        // Create a consistent chat ID regardless of parameter order
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }
    
    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        attachmentButton = findViewById(R.id.attachmentButton);
        videoCallButton = findViewById(R.id.videoCallButton);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(tutorName);
            getSupportActionBar().setSubtitle("Online"); // TODO: Show actual online status
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messagesList, currentUserId);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }
    
    private void setupButtons() {
        sendButton.setOnClickListener(v -> sendMessage());
        
        attachmentButton.setOnClickListener(v -> {
            // TODO: Implement attachment functionality
            Toast.makeText(this, "Attachment feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        videoCallButton.setOnClickListener(v -> {
            // TODO: Implement video call functionality
            Toast.makeText(this, "Video call feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // Enable send button only when there's text
        messageEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    
    private void initializeChat() {
        Log.d(TAG, "Initializing chat with ID: " + chatId);
        showLoading(true);
        
        // Check if chat already exists
        db.collection("chats")
                .document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Chat existence check completed. Exists: " + documentSnapshot.exists());
                    if (!documentSnapshot.exists()) {
                        Log.d(TAG, "Chat doesn't exist, creating new chat");
                        createChat();
                    } else {
                        Log.d(TAG, "Chat exists, loading messages");
                        loadMessages();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking chat existence: " + e.getMessage(), e);
                    if (e.getCause() != null) {
                        Log.e(TAG, "Exception cause: " + e.getCause().getMessage());
                    }
                    showLoading(false);
                    Toast.makeText(this, "Error loading chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
      private void createChat() {
        Log.d(TAG, "Creating new chat document");
        
        ChatModel chat = new ChatModel();
        chat.setId(chatId);
        chat.setTutorId(tutorId);
        chat.setStudentId(currentUserId);
        chat.setTutorName(tutorName);
        chat.setStudentName(currentUserName);
        chat.setLastMessage("");
        chat.setLastMessageTime(new Date());
        chat.setUnreadCount(0);
        
        // Set participants array for UserMessagesActivity query compatibility
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(tutorId);
        chat.setParticipants(participants);
        
        Log.d(TAG, "Chat object created with data: " + 
              "ID=" + chatId + 
              ", TutorID=" + tutorId + 
              ", StudentID=" + currentUserId + 
              ", TutorName=" + tutorName + 
              ", StudentName=" + currentUserName);
        
        db.collection("chats")
                .document(chatId)
                .set(chat)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat created successfully in Firestore");
                    loadMessages();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating chat: " + e.getMessage(), e);
                    if (e.getCause() != null) {
                        Log.e(TAG, "Exception cause: " + e.getCause().getMessage());
                    }
                    showLoading(false);
                    Toast.makeText(this, "Error creating chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    
    private void loadMessages() {
        Log.d(TAG, "Setting up real-time listener for messages in chat: " + chatId);
        
        // Set up real-time listener for messages
        messagesListener = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to messages: " + e.getMessage(), e);
                        if (e.getCause() != null) {
                            Log.e(TAG, "Exception cause: " + e.getCause().getMessage());
                        }
                        showLoading(false);
                        Toast.makeText(this, "Error loading messages: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (queryDocumentSnapshots != null) {
                        Log.d(TAG, "Received " + queryDocumentSnapshots.size() + " messages from Firestore");
                        messagesList.clear();
                        
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            ChatMessageModel message = document.toObject(ChatMessageModel.class);
                            if (message != null) {
                                message.setId(document.getId());
                                messagesList.add(message);
                                Log.d(TAG, "Added message: " + message.getMessage() + 
                                      " from " + message.getSenderName() + 
                                      " at " + message.getTimestamp());
                            } else {
                                Log.w(TAG, "Failed to parse message document: " + document.getId());
                            }
                        }
                        
                        showLoading(false);
                        updateUI();
                    } else {
                        Log.w(TAG, "queryDocumentSnapshots is null");
                        showLoading(false);
                    }
                });
    }
    
    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Log.d(TAG, "Attempted to send empty message, ignoring");
            return;
        }
        
        Log.d(TAG, "Sending message: '" + messageText + "' from user: " + currentUserName + " (" + currentUserId + ")");
          // Create message object
        ChatMessageModel message = new ChatMessageModel();
        message.setChatId(chatId);
        message.setSenderID(currentUserId);
        message.setSenderName(currentUserName);
        message.setMessage(messageText);
        message.setMessageType("text");
        message.setTimestamp(new Date());
        message.setRead(false);
        
        // Clear input
        messageEditText.setText("");
        
        // Add message to Firestore
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent successfully with ID: " + documentReference.getId());
                    // Update chat's last message
                    updateChatLastMessage(messageText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message: " + e.getMessage(), e);
                    if (e.getCause() != null) {
                        Log.e(TAG, "Exception cause: " + e.getCause().getMessage());
                    }
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Restore the message text if sending failed
                    messageEditText.setText(messageText);
                });
    }
    
    private void updateChatLastMessage(String lastMessage) {
        Log.d(TAG, "Updating chat last message to: " + lastMessage);
        
        db.collection("chats")
                .document(chatId)
                .update(
                    "lastMessage", lastMessage,
                    "lastMessageTime", new Date()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chat last message updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating chat last message: " + e.getMessage(), e);
                    if (e.getCause() != null) {
                        Log.e(TAG, "Exception cause: " + e.getCause().getMessage());
                    }
                });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            messagesRecyclerView.setVisibility(messagesList.isEmpty() ? View.GONE : View.VISIBLE);
            emptyStateText.setVisibility(messagesList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
    
    private void updateUI() {
        messageAdapter.notifyDataSetChanged();
        
        // Scroll to bottom if there are messages
        if (!messagesList.isEmpty()) {
            messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
        }
        
        // Update visibility
        messagesRecyclerView.setVisibility(messagesList.isEmpty() ? View.GONE : View.VISIBLE);
        emptyStateText.setVisibility(messagesList.isEmpty() ? View.VISIBLE : View.GONE);
        
        if (messagesList.isEmpty()) {
            emptyStateText.setText("No messages yet. Start the conversation!");
        }
    }
  
}
