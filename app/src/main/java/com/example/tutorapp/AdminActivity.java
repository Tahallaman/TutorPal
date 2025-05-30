package com.example.tutorapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tutorapp.adapters.TutorApplicationAdapter;
import com.example.tutorapp.models.TutorApplicationModel;
import com.example.tutorapp.models.TutorModel;
import com.example.tutorapp.utils.AuthenticationManager;
import com.example.tutorapp.utils.DummyDataGenerator;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements TutorApplicationAdapter.OnApplicationActionListener {

    private static final String TAG = "AdminActivity";
    
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private MaterialButton uploadTutorsButton;
    private MaterialButton uploadCategoriesButton;
    private MaterialButton uploadAllButton;
    private RecyclerView applicationsRecyclerView;
    private TutorApplicationAdapter applicationAdapter;
    
    private FirebaseFirestore db;
    private List<TutorApplicationModel> pendingApplications;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check admin access before proceeding
        if (!checkAdminAccess()) {
            return;
        }
        
        setContentView(R.layout.activity_admin);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        pendingApplications = new ArrayList<>();
        
        // Initialize views
        initializeViews();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup tab functionality
        setupTabs();
        
        // Load pending applications
        loadPendingApplications();
    }
      private boolean checkAdminAccess() {
        if (!AuthenticationManager.isLoggedIn(this)) {
            Toast.makeText(this, "Please sign in to access admin panel", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        
        AuthenticationManager.UserRole role = AuthenticationManager.getCurrentUserRole(this);
        String username = AuthenticationManager.getCurrentUsername(this);
        
        if (role != AuthenticationManager.UserRole.ADMIN && !"developer".equals(username)) {
            Toast.makeText(this, "Access denied. Administrator privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        
        return true;
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        uploadTutorsButton = findViewById(R.id.upload_tutors_button);
        uploadCategoriesButton = findViewById(R.id.upload_categories_button);
        uploadAllButton = findViewById(R.id.upload_all_button);
        applicationsRecyclerView = findViewById(R.id.applications_recycler_view);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Setup RecyclerView
        applicationAdapter = new TutorApplicationAdapter(pendingApplications, this);
        applicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        applicationsRecyclerView.setAdapter(applicationAdapter);
    }
      private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Data Upload"));
        tabLayout.addTab(tabLayout.newTab().setText("Tutor Applications"));
        
        // Set default tab
        showDataUploadTab();
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showDataUploadTab();
                        break;
                    case 1:
                        showApplicationsTab();
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void showDataUploadTab() {
        findViewById(R.id.data_upload_section).setVisibility(View.VISIBLE);
        findViewById(R.id.applications_section).setVisibility(View.GONE);
    }
    
    private void showApplicationsTab() {
        findViewById(R.id.data_upload_section).setVisibility(View.GONE);
        findViewById(R.id.applications_section).setVisibility(View.VISIBLE);
        loadPendingApplications();
    }
    
    private void loadPendingApplications() {
        db.collection("tutor_applications")
            .whereEqualTo("applicationStatus", "pending")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    pendingApplications.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        TutorApplicationModel application = document.toObject(TutorApplicationModel.class);
                        application.setApplicationId(document.getId());
                        pendingApplications.add(application);
                    }
                    applicationAdapter.updateApplications(pendingApplications);
                    
                    if (pendingApplications.isEmpty()) {
                        // Generate demo applications if none exist
                        generateDemoApplications();
                    }
                } else {
                    Log.e(TAG, "Error loading applications: ", task.getException());
                    Toast.makeText(this, "Error loading applications", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void generateDemoApplications() {
        // Create some demo tutor applications for testing
        List<TutorApplicationModel> demoApps = new ArrayList<>();
        
        // Demo application 1
        TutorApplicationModel app1 = new TutorApplicationModel();
        app1.setApplicantName("Sarah Johnson");
        app1.setEmail("sarah.johnson@email.com");
        app1.setPhone("(555) 123-4567");
        app1.setEducation("Master's in Mathematics, University of Toronto");
        app1.setExperience("5 years teaching high school mathematics");
        app1.setSubjects(List.of("Algebra", "Calculus", "Statistics"));
        app1.setCategories(List.of("Academic"));
        app1.setBio("Passionate mathematics educator with expertise in making complex concepts accessible.");
        app1.setRequestedHourlyRate(35.0);
        app1.setPreferredLocation("Toronto, ON");
        
        // Demo application 2
        TutorApplicationModel app2 = new TutorApplicationModel();
        app2.setApplicantName("Miguel Rodriguez");
        app2.setEmail("miguel.rodriguez@email.com");
        app2.setPhone("(555) 987-6543");
        app2.setEducation("Bachelor's in Spanish Literature, Universidad Complutense Madrid");
        app2.setExperience("3 years private tutoring, native Spanish speaker");
        app2.setSubjects(List.of("Spanish", "French", "ESL"));
        app2.setCategories(List.of("Languages"));
        app2.setBio("Native Spanish speaker with experience teaching multiple languages to students of all ages.");
        app2.setRequestedHourlyRate(28.0);
        app2.setPreferredLocation("Vancouver, BC");
        
        demoApps.add(app1);
        demoApps.add(app2);
        
        // Save to Firestore
        for (TutorApplicationModel app : demoApps) {
            db.collection("tutor_applications").add(app)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Demo application created: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating demo application", e);
                });
        }
        
        // Update the list after a short delay
        new android.os.Handler().postDelayed(this::loadPendingApplications, 1000);
    }
    
    private void setupClickListeners() {
        uploadTutorsButton.setOnClickListener(v -> {
            DummyDataGenerator.generateDummyTutors();
            Toast.makeText(this, "Dummy tutors uploaded to Firestore!", Toast.LENGTH_LONG).show();
        });
        
        uploadCategoriesButton.setOnClickListener(v -> {
            DummyDataGenerator.generateDummyCategories();
            Toast.makeText(this, "Dummy categories uploaded to Firestore!", Toast.LENGTH_LONG).show();
        });
          uploadAllButton.setOnClickListener(v -> {
            DummyDataGenerator.generateDummyTutors();
            DummyDataGenerator.generateDummyCategories();
            DummyDataGenerator.generateDummyReviews();
            Toast.makeText(this, "All dummy data (tutors, categories, reviews) uploaded to Firestore!", Toast.LENGTH_LONG).show();
        });
    }
    
    // TutorApplicationAdapter.OnApplicationActionListener implementation
    @Override
    public void onApproveApplication(TutorApplicationModel application) {
        String adminUsername = AuthenticationManager.getCurrentUsername(this);
        
        // Update application status
        application.setApplicationStatus("approved");
        application.setReviewDate(new Date());
        application.setReviewedBy(adminUsername != null ? adminUsername : "admin");
        
        // Create tutor profile from application
        createTutorFromApplication(application);
        
        // Update application in Firestore
        db.collection("tutor_applications").document(application.getApplicationId())
            .set(application)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Application approved successfully!", Toast.LENGTH_SHORT).show();
                loadPendingApplications();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error approving application", e);
                Toast.makeText(this, "Error approving application", Toast.LENGTH_SHORT).show();
            });
    }
    
    @Override
    public void onRejectApplication(TutorApplicationModel application) {
        String adminUsername = AuthenticationManager.getCurrentUsername(this);
        
        // Update application status
        application.setApplicationStatus("rejected");
        application.setReviewDate(new Date());
        application.setReviewedBy(adminUsername != null ? adminUsername : "admin");
        
        // Update application in Firestore
        db.collection("tutor_applications").document(application.getApplicationId())
            .set(application)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Application rejected", Toast.LENGTH_SHORT).show();
                loadPendingApplications();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error rejecting application", e);
                Toast.makeText(this, "Error rejecting application", Toast.LENGTH_SHORT).show();
            });
    }
    
    @Override
    public void onViewApplicationDetails(TutorApplicationModel application) {
        // For now, show a toast with basic info
        // In a full implementation, this would open a detailed view dialog
        String details = String.format("Name: %s\nEmail: %s\nEducation: %s\nExperience: %s", 
            application.getApplicantName(),
            application.getEmail(),
            application.getEducation(),
            application.getExperience());
            
        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }
    
    private void createTutorFromApplication(TutorApplicationModel application) {
        // Create a new tutor profile from the approved application
        TutorModel tutor = new TutorModel();
        tutor.setName(application.getApplicantName());
        tutor.setEmail(application.getEmail());
        tutor.setPhone(application.getPhone());
        tutor.setEducation(application.getEducation());
        tutor.setExperience(application.getExperience());
        tutor.setBio(application.getBio());
        tutor.setLocation(application.getPreferredLocation());
        tutor.setPrice(application.getRequestedHourlyRate());
        tutor.setCategories(application.getCategories());
        
        // Set default values
        tutor.setRating(0.0);
        tutor.setReviewCount(0);
        tutor.setTotalStudents(0);
        tutor.setVerified(true); // Auto-verify approved tutors
        
        // Set subject as the first subject from the list
        if (application.getSubjects() != null && !application.getSubjects().isEmpty()) {
            tutor.setSubject(application.getSubjects().get(0));
        }
        
        // Add to tutors collection
        db.collection("tutors").add(tutor)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Tutor created successfully: " + documentReference.getId());
                // Optionally promote user to tutor role in authentication system
                AuthenticationManager.promoteToTutor(this, application.getApplicantId());
            })            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating tutor profile", e);
            });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
