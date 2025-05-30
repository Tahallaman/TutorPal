package com.example.tutorapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tutorapp.R;
import com.example.tutorapp.models.TutorApplicationModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying tutor applications in the admin panel.
 */
public class TutorApplicationAdapter extends RecyclerView.Adapter<TutorApplicationAdapter.ApplicationViewHolder> {
    
    private List<TutorApplicationModel> applications;
    private OnApplicationActionListener actionListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnApplicationActionListener {
        void onApproveApplication(TutorApplicationModel application);
        void onRejectApplication(TutorApplicationModel application);
        void onViewApplicationDetails(TutorApplicationModel application);
    }
    
    public TutorApplicationAdapter(List<TutorApplicationModel> applications, OnApplicationActionListener listener) {
        this.applications = applications;
        this.actionListener = listener;
    }
    
    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutor_application, parent, false);
        return new ApplicationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        TutorApplicationModel application = applications.get(position);
        
        holder.applicantNameText.setText(application.getApplicantName());
        holder.emailText.setText(application.getEmail());
        holder.applicationDateText.setText("Applied: " + dateFormat.format(application.getApplicationDate()));
        holder.statusText.setText(application.getApplicationStatus().toUpperCase());
        
        // Set status color
        int statusColor;
        switch (application.getApplicationStatus().toLowerCase()) {
            case "approved":
                statusColor = R.color.success;
                break;
            case "rejected":
                statusColor = R.color.error;
                break;
            default:
                statusColor = R.color.warning;
                break;
        }
        holder.statusText.setTextColor(holder.itemView.getContext().getColor(statusColor));
        
        // Set subjects
        if (application.getSubjects() != null && !application.getSubjects().isEmpty()) {
            holder.subjectsText.setText("Subjects: " + String.join(", ", application.getSubjects()));
            holder.subjectsText.setVisibility(View.VISIBLE);
        } else {
            holder.subjectsText.setVisibility(View.GONE);
        }
        
        // Set hourly rate
        if (application.getRequestedHourlyRate() > 0) {
            holder.hourlyRateText.setText(String.format(Locale.getDefault(), "Requested rate: $%.2f/hour", 
                    application.getRequestedHourlyRate()));
            holder.hourlyRateText.setVisibility(View.VISIBLE);
        } else {
            holder.hourlyRateText.setVisibility(View.GONE);
        }
        
        // Show/hide action buttons based on status
        boolean isPending = "pending".equals(application.getApplicationStatus().toLowerCase());
        holder.approveButton.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.rejectButton.setVisibility(isPending ? View.VISIBLE : View.GONE);
        
        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onViewApplicationDetails(application);
            }
        });
        
        holder.approveButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onApproveApplication(application);
            }
        });
        
        holder.rejectButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRejectApplication(application);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return applications.size();
    }
    
    public void updateApplications(List<TutorApplicationModel> newApplications) {
        this.applications = newApplications;
        notifyDataSetChanged();
    }
    
    public static class ApplicationViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView applicantNameText;
        TextView emailText;
        TextView applicationDateText;
        TextView statusText;
        TextView subjectsText;
        TextView hourlyRateText;
        MaterialButton approveButton;
        MaterialButton rejectButton;
        
        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            applicantNameText = itemView.findViewById(R.id.applicant_name);
            emailText = itemView.findViewById(R.id.applicant_email);
            applicationDateText = itemView.findViewById(R.id.application_date);
            statusText = itemView.findViewById(R.id.application_status);
            subjectsText = itemView.findViewById(R.id.subjects_text);
            hourlyRateText = itemView.findViewById(R.id.hourly_rate_text);
            approveButton = itemView.findViewById(R.id.approve_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}
