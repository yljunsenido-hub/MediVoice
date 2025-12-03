package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class Monitoring extends AppCompatActivity {

    private LinearLayout monitoringContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        monitoringContainer = findViewById(R.id.monitoringContainer);

        loadObservationList();
    }

    private void loadObservationList() {
        String caregiverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Caregiver")
                .child(caregiverId)
                .child("Observations")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        addEmptyMessage();
                        return;
                    }

                    for (DataSnapshot obs : snapshot.getChildren()) {

                        String elderName = obs.child("elderName").getValue(String.class);
                        String observationId = obs.child("observationId").getValue(String.class);
                        String nurseId = obs.child("nurseId").getValue(String.class); // fetch nurseId

                        addElderButton(elderName, observationId, nurseId);
                    }
                });
    }

    private void addEmptyMessage() {
        TextView tv = new TextView(this);
        tv.setText("No observations found.");
        tv.setTextSize(18);
        tv.setPadding(20, 20, 20, 20);
        monitoringContainer.addView(tv);
    }

    private void addElderButton(String elderName, String obsId, String nurseId) {

        Button btn = new Button(this);
        btn.setText(elderName);
        btn.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 25);
        btn.setLayoutParams(params);

        // Pass elderName, elderId, and nurseId to details activity
        btn.setOnClickListener(v -> {
            Intent i = new Intent(Monitoring.this, MonitoringDetailsActivity.class);
            i.putExtra("elderId", obsId);       // observationId
            i.putExtra("elderName", elderName);
            i.putExtra("nurseId", nurseId);     // nurse who assigned this elder
            startActivity(i);
        });

        monitoringContainer.addView(btn);
    }
}
