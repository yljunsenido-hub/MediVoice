package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MedWelcome extends AppCompatActivity {

    Button btnCaregiver, btnNurse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnCaregiver = findViewById(R.id.btnCaregiver);
        btnNurse = findViewById(R.id.btnNurse);

        btnCaregiver.setOnClickListener(v -> {
            Intent caregiver = new Intent(MedWelcome.this, MedCaregiverLogin.class);
            startActivity(caregiver);
            finish();
        });

        btnNurse.setOnClickListener(v -> {
            Intent nurse = new Intent(MedWelcome.this, MedNurseLogin.class);
            startActivity(nurse);
            finish();
        });
    }
}