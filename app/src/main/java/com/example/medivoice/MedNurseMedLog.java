package com.example.medivoice;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class MedNurseMedLog extends AppCompatActivity {

    LinearLayout medList;
    BottomNavigationView bottomNav;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_med_nurse_med_log);

        medList = findViewById(R.id.medicationStatusList);
        bottomNav = findViewById(R.id.bottomNav);
        btnBack = findViewById(R.id.btnBack);

        bottomNav.setSelectedItemId(R.id.nav_medLog);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_medLog) {
                return true; // already here
            } else if (id == R.id.nav_elderlyStatLog) {
                startActivity(new Intent(this, ElderList.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, NurseProfile.class));
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, MedNurseHomepage.class));
                return true;
            }
            return false;
        });

        btnBack.setOnClickListener(v ->
                startActivity(new Intent(this, MedNurseHomepage.class))
        );

        FirebaseDatabase.getInstance().getReference()
                .child("MedicationLog")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
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

                            CardView card = new CardView(MedNurseMedLog.this);

                            LinearLayout.LayoutParams params =
                                    new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);

                            params.setMargins(0, 0, 0, 30);
                            card.setLayoutParams(params);
                            card.setRadius(20f);
                            card.setCardElevation(10f);

                            if ("pending".equals(status)) {
                                card.setCardBackgroundColor(Color.parseColor("#D32F2F"));
                            } else if ("confirmed".equals(status)) {
                                card.setCardBackgroundColor(Color.parseColor("#388E3C"));
                            } else {
                                card.setCardBackgroundColor(Color.GRAY);
                            }

                            LinearLayout inside = new LinearLayout(MedNurseMedLog.this);
                            inside.setOrientation(LinearLayout.VERTICAL);
                            inside.setPadding(30, 30, 30, 30);

                            TextView tv = new TextView(MedNurseMedLog.this);

                            tv.setText(
                                    "Elder: " + elder +
                                            "\nMedicine: " + med +
                                            "\nDosage: " + dosage +
                                            "\nSchedule: " + schedule +
                                            "\nTime: " + time
                            );

                            tv.setTextColor(Color.WHITE);
                            tv.setTextSize(18);

                            inside.addView(tv);
                            card.addView(inside);
                            medList.addView(card);
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    }
                });
    }
}