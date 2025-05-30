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
import com.example.tutorapp.models.ConversationModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying conversations in the UserMessagesActivity
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    
    private Context context;
    private List<ConversationModel> conversations;
    private OnConversationClickListener clickListener;
    private SimpleDateFormat dateFormat;
    
    public interface OnConversationClickListener {
        void onConversationClick(ConversationModel conversation);
    }
    
    public ConversationAdapter(Context context, List<ConversationModel> conversations, 
                             OnConversationClickListener clickListener) {
        this.context = context;
        this.conversations = conversations;
        this.clickListener = clickListener;
        this.dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ConversationModel conversation = conversations.get(position);
        holder.bind(conversation);
    }
    
    @Override
    public int getItemCount() {
        return conversations.size();
    }
    
    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private ImageView tutorImageView;
        private TextView tutorNameTextView;
        private TextView lastMessageTextView;
        private TextView timeTextView;
        private View unreadIndicator;
        
        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            tutorImageView = itemView.findViewById(R.id.tutorImageView);
            tutorNameTextView = itemView.findViewById(R.id.tutorNameTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onConversationClick(conversations.get(position));
                }
            });
        }
        
        public void bind(ConversationModel conversation) {
            tutorNameTextView.setText(conversation.getOtherParticipantName() != null ? 
                                    conversation.getOtherParticipantName() : "Unknown Tutor");
            
            lastMessageTextView.setText(conversation.getLastMessage() != null ? 
                                      conversation.getLastMessage() : "No messages yet");
            
            if (conversation.getLastMessageTime() != null) {
                timeTextView.setText(dateFormat.format(conversation.getLastMessageTime()));
            } else {
                timeTextView.setText("");
            }
            
            unreadIndicator.setVisibility(conversation.isHasUnreadMessages() ? 
                                        View.VISIBLE : View.GONE);
            
            // TODO: Load tutor image using Glide or Picasso
            // For now, use a placeholder
            tutorImageView.setImageResource(R.drawable.ic_person_placeholder);
        }
    }
}
