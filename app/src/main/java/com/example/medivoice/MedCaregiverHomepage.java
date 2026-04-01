package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MedCaregiverHomepage extends AppCompatActivity {

    private LinearLayout medTracker, monitoring, textToSpeech, runningNotes;
    private ImageView logout;
    private FloatingActionButton fabEmergency;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_caregiver_homepage);

        setupWindowInsets();
        initFirebase();

        if (!isUserLoggedIn()) return;

        initViews();
        setupClickListeners();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private boolean isUserLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return false;
        }
        return true;
    }

    private void initViews() {
        medTracker = findViewById(R.id.medTracker);
        monitoring = findViewById(R.id.monitoring);
        textToSpeech = findViewById(R.id.textToSpeech);
        runningNotes = findViewById(R.id.runningNotes);
        logout = findViewById(R.id.logout);
        fabEmergency = findViewById(R.id.fabEmergency);
    }

    private void setupClickListeners() {

        medTracker.setOnClickListener(v ->
                startActivity(new Intent(this, MedicationTracker.class)));

        monitoring.setOnClickListener(v ->
                startActivity(new Intent(this, Monitoring.class)));

        textToSpeech.setOnClickListener(v ->
                startActivity(new Intent(this, MedCareTextToSpeech.class)));

        runningNotes.setOnClickListener(v ->
                startActivity(new Intent(this, MedCaregiverRunningnotes.class)));

        fabEmergency.setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyElderListActivity.class)));

        logout.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MedCaregiverLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}