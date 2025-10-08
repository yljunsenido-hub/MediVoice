package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GuardianHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guardian_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button prescriptionButton = findViewById(R.id.prescriptionButton);
        Button voiceButton = findViewById(R.id.voiceButton);

//        prescriptionButton.setOnClickListener(v -> {
//            Intent intent = new Intent(GuardianHomePage.this, PrescriptionScanner.class);
//            startActivity(intent);
//        });

        voiceButton.setOnClickListener(v -> {
            Intent voiceIntent = new Intent(GuardianHomePage.this, GuardianRecordSpeechToText.class);
            startActivity(voiceIntent);
        });
    }
}