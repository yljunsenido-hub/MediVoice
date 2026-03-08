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

import java.util.Map;
import java.util.TreeMap;

public class ElderStatusLogs extends AppCompatActivity {

    LinearLayout statusList;
    BottomNavigationView bottomNav;
    ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_elder_status_logs);

        // Initialize views
        imgBack = findViewById(R.id.imgBack);
        statusList = findViewById(R.id.statusList);
        bottomNav = findViewById(R.id.bottomNav);

        // Highlight Elder Status Logs in bottom navigation
        bottomNav.setSelectedItemId(R.id.nav_elderlyStatLog);

        // Load elder logs
        loadStatusLogs();

        // Setup back button
        setupBackButton();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void setupBackButton() {
        imgBack.setOnClickListener(v -> {
            startActivity(new Intent(ElderStatusLogs.this, MedNurseHomepage.class));
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_elderlyStatLog) {
                return true;
            } else if (id == R.id.nav_medLog) {
                startActivity(new Intent(ElderStatusLogs.this, MedNurseMedLog.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(ElderStatusLogs.this, MedNurseHomepage.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(ElderStatusLogs.this, NurseProfile.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadStatusLogs() {
        FirebaseDatabase.getInstance().getReference()
                .child("Elder")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        statusList.removeAllViews();

                        for (DataSnapshot elderSnap : snapshot.getChildren()) {
                            String elderId = elderSnap.getKey();

                            DataSnapshot vitalsSnap = elderSnap.child("MonitoringRecords").child("VitalSigns");
                            Map<String, Object> latestVitals = getLatestRecord(vitalsSnap);

                            DataSnapshot outputSnap = elderSnap.child("MonitoringRecords").child("OutputMonitoring");
                            Map<String, Object> latestOutput = getLatestRecord(outputSnap);

                            String elderName = "Unknown";
                            if (latestVitals.get("elderName") != null) {
                                elderName = latestVitals.get("elderName").toString();
                            } else if (latestOutput.get("elderName") != null) {
                                elderName = latestOutput.get("elderName").toString();
                            }

                            // Create card
                            CardView card = new CardView(ElderStatusLogs.this);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 0, 0, 30);
                            card.setLayoutParams(params);
                            card.setRadius(20f);
                            card.setCardElevation(10f);
                            card.setCardBackgroundColor(Color.parseColor("#1976D2"));

                            LinearLayout inside = new LinearLayout(ElderStatusLogs.this);
                            inside.setOrientation(LinearLayout.VERTICAL);
                            inside.setPadding(30, 30, 30, 30);

                            TextView tvElder = new TextView(ElderStatusLogs.this);
                            tvElder.setText("Elder: " + elderName);
                            tvElder.setTextColor(Color.WHITE);
                            tvElder.setTextSize(20f);
                            inside.addView(tvElder);

                            TextView tvVitals = new TextView(ElderStatusLogs.this);
                            tvVitals.setText(
                                    "Vital Signs:\n" +
                                            "Blood Pressure: " + getValue(latestVitals, "bloodPressure") + "\n" +
                                            "Blood Temperature: " + getValue(latestVitals, "temperature") + "\n" +
                                            "Pulse Rate: " + getValue(latestVitals, "pulseRate") + "\n" +
                                            "Respiratory Rate: " + getValue(latestVitals, "respiratoryRate") + "\n" +
                                            "Oxygen Saturation: " + getValue(latestVitals, "oxygenSaturation") + "\n" +
                                            "Blood Sugar: " + getValue(latestVitals, "bloodSugar")
                            );
                            tvVitals.setTextColor(Color.WHITE);
                            tvVitals.setTextSize(16f);
                            inside.addView(tvVitals);

                            TextView tvOutput = new TextView(ElderStatusLogs.this);
                            tvOutput.setText(
                                    "\nOutput Monitoring (Optional):\n" +
                                            "Urine Output: " + getValue(latestOutput, "urineOutput") + "\n" +
                                            "Stool: " + getValue(latestOutput, "stool")
                            );
                            tvOutput.setTextColor(Color.WHITE);
                            tvOutput.setTextSize(16f);
                            inside.addView(tvOutput);

                            card.addView(inside);
                            statusList.addView(card);
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {}
                });
    }

    private Map<String, Object> getLatestRecord(DataSnapshot snap) {
        Map<String, Object> latest = new TreeMap<>();
        long maxTime = -1;
        for (DataSnapshot child : snap.getChildren()) {
            Long t = child.child("timestamp").getValue(Long.class);
            if (t != null && t > maxTime) {
                maxTime = t;
                latest.clear();
                for (DataSnapshot field : child.getChildren()) {
                    latest.put(field.getKey(), field.getValue());
                }
            }
        }
        return latest;
    }

    private String getValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null || val.toString().isEmpty()) return "N/A";
        return val.toString();
    }
}