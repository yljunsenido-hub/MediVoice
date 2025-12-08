package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import com.example.medivoice.R;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MedNurseHomepage extends AppCompatActivity {

    LinearLayout elderRegister, prescription, runningNotes;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_nurse_homepage);

        // Enable EdgeToEdge (optional)

        // Safely get root view
        LinearLayout rootLayout = findViewById(R.id.main);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize clickable items
        prescription = findViewById(R.id.prescription);
        elderRegister = findViewById(R.id.elderRegister);
        runningNotes = findViewById(R.id.runningNotes);
        bottomNav = findViewById(R.id.bottomNav);
//        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Set click listeners
        prescription.setOnClickListener(v ->
                startActivity(new Intent(MedNurseHomepage.this, MedNursePrescription.class))
        );

        elderRegister.setOnClickListener(v ->
                startActivity(new Intent(MedNurseHomepage.this, ElderRegistration.class))
        );

        runningNotes.setOnClickListener(v ->
                startActivity(new Intent(MedNurseHomepage.this, MedNurseRunningNote.class))
        );

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true; // Already home
//            } else if (id == R.id.nav_medLog) {
//                startActivity(new Intent(MedNurseHomepage.this, MedNursePrescription.class));
//                return true;
//            } else if (id == R.id.nav_elderlyStatLog) {
//                startActivity(new Intent(MedNurseHomepage.this, ElderList.class));
//                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MedNurseHomepage.this, NurseProfile.class));
                return true;
            }
            return false;
        });
    }
}