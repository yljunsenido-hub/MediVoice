package com.example.medivoice;

import android.content.Intent;
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

import java.util.Map;
import java.util.TreeMap;

public class ElderStatusLogs extends AppCompatActivity {

    private LinearLayout statusList;
    private BottomNavigationView bottomNav;
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_elder_status_logs);

        initViews();
        setupBackButton();
        setupBottomNavigation();
        loadStatusLogs();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        statusList = findViewById(R.id.statusList);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_elderlyStatLog);
    }

    private void setupBackButton() {
        imgBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MedNurseHomepage.class));
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_elderlyStatLog) return true;
            if (id == R.id.nav_medLog) startActivity(new Intent(this, MedNurseMedLog.class));
            if (id == R.id.nav_home) startActivity(new Intent(this, MedNurseHomepage.class));
            if (id == R.id.nav_profile) startActivity(new Intent(this, NurseProfile.class));

            finish();
            return true;
        });
    }

    private void loadStatusLogs() {
        FirebaseDatabase.getInstance().getReference("Elder")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        statusList.removeAllViews();

                        for (DataSnapshot elderSnap : snapshot.getChildren()) {

                            DataSnapshot vitalsSnap = elderSnap.child("MonitoringRecords").child("VitalSigns");
                            Map<String, Object> latestVitals = getLatestRecord(vitalsSnap);

                            DataSnapshot outputSnap = elderSnap.child("MonitoringRecords").child("OutputMonitoring");
                            Map<String, Object> latestOutput = getLatestRecord(outputSnap);

                            String elderName = getValue(latestVitals, "elderName");
                            if (elderName.equals("N/A")) {
                                elderName = getValue(latestOutput, "elderName");
                            }

                            addStatusCard(elderName, latestVitals, latestOutput);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void addStatusCard(String elderName, Map<String, Object> vitals, Map<String, Object> output) {

        View view = getLayoutInflater().inflate(R.layout.item_elder_status, statusList, false);

        CardView card = view.findViewById(R.id.cardView);
        TextView tvElder = view.findViewById(R.id.tvElder);
        TextView tvVitals = view.findViewById(R.id.tvVitals);
        TextView tvOutput = view.findViewById(R.id.tvOutput);

        tvElder.setText(elderName);

        tvVitals.setText(
                "Vital Signs\n" +
                        "Blood Pressure: " + getValue(vitals, "bloodPressure") + "\n" +
                        "Temperature: " + getValue(vitals, "temperature") + "\n" +
                        "Pulse Rate: " + getValue(vitals, "pulseRate") + "\n" +
                        "Respiratory Rate: " + getValue(vitals, "respiratoryRate") + "\n" +
                        "Oxygen Saturation: " + getValue(vitals, "oxygenSaturation") + "\n" +
                        "Blood Sugar: " + getValue(vitals, "bloodSugar")
        );

        tvOutput.setText(
                "Output Monitoring\n" +
                        "Urine Output: " + getValue(output, "urineOutput") + "\n" +
                        "Stool: " + getValue(output, "stool")
        );

        statusList.addView(view);
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
        return (val == null || val.toString().isEmpty()) ? "N/A" : val.toString();
    }
}