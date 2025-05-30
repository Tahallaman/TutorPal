package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.adapters.ConversationAdapter;
import com.example.tutorapp.models.ConversationModel;
import com.example.tutorapp.utils.AuthenticationManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity to display user's conversations/messages without requiring tutor-specific context
 */
public class UserMessagesActivity extends AppCompatActivity {
    private static final String TAG = "UserMessagesActivity";
    
    private FirebaseFirestore db;
    private RecyclerView conversationsRecyclerView;
    private ConversationAdapter conversationAdapter;
    private List<ConversationModel> conversationsList;
    private TextView emptyStateText;
      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_messages);
        
        // Check authentication first
        if (!AuthenticationManager.isLoggedIn(this)) {
            Toast.makeText(this, "Please sign in to access messages", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        db = FirebaseFirestore.getInstance();
        conversationsList = new ArrayList<>();
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadUserConversations();
    }
    
    private void initializeViews() {
        conversationsRecyclerView = findViewById(R.id.conversationsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Messages");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(this, conversationsList, this::openChat);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationsRecyclerView.setAdapter(conversationAdapter);
    }
      private void loadUserConversations() {
        // Get current user ID from authentication
        String currentUserId = AuthenticationManager.getCurrentUserId(this);
        
        if (currentUserId == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }        // First get conversations where user is a participant
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    conversationsList.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }
                      for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ConversationModel conversation = document.toObject(ConversationModel.class);
                        conversation.setId(document.getId());
                        conversationsList.add(conversation);
                    }
                      // Sort conversations by last message time (most recent first)
                    conversationsList.sort((c1, c2) -> {
                        Date time1 = c1.getLastMessageTime();
                        Date time2 = c2.getLastMessageTime();
                        
                        // Handle null values (put them at the end)
                        if (time1 == null && time2 == null) return 0;
                        if (time1 == null) return 1;
                        if (time2 == null) return -1;
                        
                        // Sort in descending order (most recent first)
                        return time2.compareTo(time1);
                    });
                    
                    conversationAdapter.notifyDataSetChanged();
                    showConversations();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load conversations: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }
    
    private void showEmptyState() {
        conversationsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("No conversations yet.\nContact a tutor to start messaging.");
    }
    
    private void showConversations() {
        conversationsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
    }
    
    private void openChat(ConversationModel conversation) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("conversation_id", conversation.getId());
        intent.putExtra("tutor_id", conversation.getOtherParticipantId());
        intent.putExtra("tutor_name", conversation.getOtherParticipantName());
        startActivity(intent);
    }
}
