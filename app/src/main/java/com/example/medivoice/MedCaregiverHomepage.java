package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MedCaregiverHomepage extends AppCompatActivity {

    LinearLayout medTracker, monitoring, textToSpeech, runningNotes;
    FloatingActionButton fabEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_caregiver_homepage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize LinearLayouts as clickable cards
        medTracker = findViewById(R.id.medTracker);
        monitoring = findViewById(R.id.monitoring);
        textToSpeech = findViewById(R.id.textToSpeech);
        runningNotes = findViewById(R.id.runningNotes);

        fabEmergency = findViewById(R.id.fabEmergency);

        // Set click listeners
        medTracker.setOnClickListener(v -> startActivity(new Intent(MedCaregiverHomepage.this, MedicationTracker.class)));
        monitoring.setOnClickListener(v -> startActivity(new Intent(MedCaregiverHomepage.this, Monitoring.class)));
        textToSpeech.setOnClickListener(v -> startActivity(new Intent(MedCaregiverHomepage.this, MedCareTextToSpeech.class)));
        runningNotes.setOnClickListener(v -> {
            startActivity(new Intent(MedCaregiverHomepage.this, MedCaregiverRunningnotes.class));
        });




        fabEmergency.setOnClickListener(v -> startActivity(new Intent(MedCaregiverHomepage.this, EmergencyElderListActivity.class)));
    }
}
