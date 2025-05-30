package com.example.tutorapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.R;
import com.example.tutorapp.models.BookingModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying bookings in the UserBookingsActivity
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    
    private Context context;
    private List<BookingModel> bookings;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    
    public BookingAdapter(Context context, List<BookingModel> bookings) {
        this.context = context;
        this.bookings = bookings;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking = bookings.get(position);
        holder.bind(booking);
    }
    
    @Override
    public int getItemCount() {
        return bookings.size();
    }
    
    class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tutorNameTextView;
        private TextView subjectTextView;
        private TextView dateTextView;
        private TextView timeTextView;
        private TextView statusTextView;
        private TextView locationTextView;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tutorNameTextView = itemView.findViewById(R.id.tutorNameTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
        }
        
        public void bind(BookingModel booking) {
            tutorNameTextView.setText(booking.getTutorName() != null ? 
                                    booking.getTutorName() : "Unknown Tutor");
            
            subjectTextView.setText(booking.getSubject() != null ? 
                                  booking.getSubject() : "Subject not specified");
            
            if (booking.getBookingDate() != null) {
                dateTextView.setText(dateFormat.format(booking.getBookingDate()));
            } else {
                dateTextView.setText("Date TBD");
            }
            
            if (booking.getStartTime() != null && booking.getEndTime() != null) {
                String timeText = timeFormat.format(booking.getStartTime()) + 
                                " - " + timeFormat.format(booking.getEndTime());
                timeTextView.setText(timeText);
            } else {
                timeTextView.setText("Time TBD");
            }
            
            statusTextView.setText(booking.getStatus() != null ? 
                                 booking.getStatus().toUpperCase() : "PENDING");
            
            // Set status color based on status
            String status = booking.getStatus();
            if ("confirmed".equalsIgnoreCase(status)) {
                statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else if ("cancelled".equalsIgnoreCase(status)) {
                statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            }
            
            locationTextView.setText(booking.getLocation() != null ? 
                                   booking.getLocation() : "Location TBD");
        }
    }
}
