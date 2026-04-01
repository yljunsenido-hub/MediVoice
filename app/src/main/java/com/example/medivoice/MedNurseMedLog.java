package com.example.medivoice;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

public class MedNurseMedLog extends AppCompatActivity {

    private LinearLayout medList;
    private BottomNavigationView bottomNav;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_med_nurse_med_log);

        initViews();
        setupBottomNav();
        setupBackButton();
        loadMedicationLogs();
    }

    private void initViews() {
        medList = findViewById(R.id.medicationStatusList);
        bottomNav = findViewById(R.id.bottomNav);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_medLog);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_medLog) {
                return true;
            } else if (id == R.id.nav_elderlyStatLog) {
                startActivity(new Intent(this, ElderStatusLogs.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, NurseProfile.class));
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, MedNurseHomepage.class));
            }
            return true;
        });
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v ->
                startActivity(new Intent(this, MedNurseHomepage.class))
        );
    }

    private void loadMedicationLogs() {
        FirebaseDatabase.getInstance().getReference("MedicationLog")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        medList.removeAllViews();

                        for (DataSnapshot child : snapshot.getChildren()) {

                            String elder = child.child("elderName").getValue(String.class);
                            String med = child.child("medicationName").getValue(String.class);
                            String dosage = child.child("dosage").getValue(String.class);
                            String schedule = child.child("schedule").getValue(String.class);
                            String time = child.child("time").getValue(String.class);
                            String status = child.child("status").getValue(String.class);

                            addMedCard(elder, med, dosage, schedule, time, status);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
    }

    private void addMedCard(String elder, String med, String dosage,
                            String schedule, String time, String status) {

        View view = getLayoutInflater().inflate(R.layout.item_med_log, medList, false);

        CardView card = view.findViewById(R.id.cardView);
        TextView tvElder = view.findViewById(R.id.tvElder);
        TextView tvMedicine = view.findViewById(R.id.tvMedicine);
        TextView tvDosage = view.findViewById(R.id.tvDosage);
        TextView tvSchedule = view.findViewById(R.id.tvSchedule);
        TextView tvTime = view.findViewById(R.id.tvTime);

        tvElder.setText(safe(elder));
        tvMedicine.setText("Medicine: " + safe(med));
        tvDosage.setText("Dosage: " + safe(dosage));
        tvSchedule.setText("Schedule: " + safe(schedule));
        tvTime.setText("Time: " + safe(time));

        if ("pending".equals(status)) {
            card.setCardBackgroundColor(Color.parseColor("#D32F2F"));
        } else if ("confirmed".equals(status)) {
            card.setCardBackgroundColor(Color.parseColor("#388E3C"));
        } else {
            card.setCardBackgroundColor(Color.GRAY);
        }

        medList.addView(view);
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}