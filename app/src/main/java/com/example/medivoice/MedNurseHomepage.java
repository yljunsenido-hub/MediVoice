package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MedNurseHomepage extends AppCompatActivity {

    Button elderRegister, prescription, elderStatusLog,nurseProfile,runningNotes;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_med_nurse_homepage);

        // Handle system bars insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        elderRegister = findViewById(R.id.elderRegister);
        prescription = findViewById(R.id.prescription);
        elderStatusLog = findViewById(R.id.elderStatusLog);
        nurseProfile = findViewById(R.id.nurseProfile);
        runningNotes = findViewById(R.id.runningNotes);







//        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                int itemId = item.getItemId();
//
//                if (itemId == R.id.nav_home) {
//                    // Stay on Home
//                    return true;
//                } else if (itemId == R.id.nav_medLog) {
//                    startActivity(new Intent(getApplicationContext(), .class));
//                    overridePendingTransition(0, 0);
//                    return true;
//            } else if (itemId == R.id.nav_mic) {
//                 startActivity(new Intent(getApplicationContext(), GuardianRecordActivity.class));
//               overridePendingTransition(0, 0);
//              return true;
//                } else if (itemId == R.id.nav_profile) {
//                    startActivity(new Intent(getApplicationContext(), .class));
//                    overridePendingTransition(0, 0);
//                    return true;
//                }
//
//                return false;
//            }
//        });

        elderRegister.setOnClickListener(v -> startActivity(new Intent(MedNurseHomepage.this, ElderRegistration.class)));
        prescription.setOnClickListener(v -> startActivity(new Intent(MedNurseHomepage.this, MedNursePrescription.class)));
        nurseProfile.setOnClickListener(v -> startActivity(new Intent(MedNurseHomepage.this, NurseProfile.class)));
        runningNotes.setOnClickListener(v -> startActivity(new Intent(MedNurseHomepage.this, MedNurseRunningNote.class)));
//        elderStatusLog.setOnClickListener(v -> startActivity(new Intent(MedNurseHomepage.this, ElderStatusLogActivity.class)));
    } }