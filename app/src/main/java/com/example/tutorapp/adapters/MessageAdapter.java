package com.example.tutorapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.R;
import com.example.tutorapp.models.ChatMessageModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private Context context;
    private List<ChatMessageModel> messages;
    private String currentUserId;
    private SimpleDateFormat timeFormat;
    
    public MessageAdapter(Context context, List<ChatMessageModel> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    }
      @Override
    public int getItemViewType(int position) {
        ChatMessageModel message = messages.get(position);
        if (message.getSenderID().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageModel message = messages.get(position);
        
        if (holder instanceof SentMessageViewHolder) {
            bindSentMessage((SentMessageViewHolder) holder, message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            bindReceivedMessage((ReceivedMessageViewHolder) holder, message);
        }
    }
    
    private void bindSentMessage(SentMessageViewHolder holder, ChatMessageModel message) {
        holder.messageText.setText(message.getMessage());
        
        if (message.getTimestamp() != null) {
            holder.timeText.setText(timeFormat.format(message.getTimestamp()));
        }
        
        // Show read status
        if (message.isRead()) {
            holder.statusIcon.setImageResource(R.drawable.ic_message_read);
        } else {
            holder.statusIcon.setImageResource(R.drawable.ic_message_sent);
        }
        
        // Handle different message types
        if ("image".equals(message.getMessageType())) {
            holder.messageText.setVisibility(View.GONE);
            // TODO: Show image attachment
        } else {
            holder.messageText.setVisibility(View.VISIBLE);
        }
    }
    
    private void bindReceivedMessage(ReceivedMessageViewHolder holder, ChatMessageModel message) {
        holder.messageText.setText(message.getMessage());
        holder.senderName.setText(message.getSenderName());
        
        if (message.getTimestamp() != null) {
            holder.timeText.setText(timeFormat.format(message.getTimestamp()));
        }
        
        // Handle different message types
        if ("image".equals(message.getMessageType())) {
            holder.messageText.setVisibility(View.GONE);
            // TODO: Show image attachment
        } else {
            holder.messageText.setVisibility(View.VISIBLE);
        }
        
        // TODO: Load sender profile image
        holder.profileImage.setImageResource(R.drawable.ic_tutor_placeholder);
    }
    
    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }
    
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView statusIcon;
        
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }
    }
    
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, senderName;
        ImageView profileImage;
        
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
            senderName = itemView.findViewById(R.id.senderName);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
}
