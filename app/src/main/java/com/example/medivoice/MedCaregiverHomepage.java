package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import the FAB class

public class MedCaregiverHomepage extends AppCompatActivity {

    Button medTracker, monitoring, textToSpeech;
    FloatingActionButton fabEmergency; // Declaration for the FAB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_med_caregiver_homepage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        medTracker = findViewById(R.id.medTracker);
        monitoring = findViewById(R.id.monitoring);
        textToSpeech = findViewById(R.id.textToSpeech);
        fabEmergency = findViewById(R.id.fabEmergency); // Initialization for the FAB


        medTracker.setOnClickListener(v -> startActivity(new Intent(MedCaregiverHomepage.this, MedicationTracker.class)));
        monitoring.setOnClickListener(v -> startActivity(new Intent(MedCaregiverHomepage.this, Monitoring.class)));
        textToSpeech.setOnClickListener(v -> startActivity(new Intent(MedCaregiverHomepage.this, MedCareTextToSpeech.class)));

        // 🚨 New FAB Click Listener 🚨
        fabEmergency.setOnClickListener(v ->
                startActivity(new Intent(MedCaregiverHomepage.this, EmergencyElderListActivity.class))
        );

    }
}