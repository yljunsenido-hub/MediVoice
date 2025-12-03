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

public class MedicationTracker extends AppCompatActivity {

    private LinearLayout medicationsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_tracker);

        medicationsContainer = findViewById(R.id.medicationsContainer);

        loadPrescriptions();
    }

    private void loadPrescriptions() {
        String caregiverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Caregiver")
                .child(caregiverId)
                .child("Prescriptions")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        addEmptyMessage();
                        return;
                    }

                    for (DataSnapshot pres : snapshot.getChildren()) {

                        String elderName = pres.child("elderName").getValue(String.class);
                        String presId = pres.child("prescriptionId").getValue(String.class);

                        addElderButton(elderName, presId);
                    }

                });
    }

    private void addEmptyMessage() {
        TextView tv = new TextView(this);
        tv.setText("No prescriptions found.");
        tv.setTextSize(18);
        tv.setPadding(20, 20, 20, 20);
        medicationsContainer.addView(tv);
    }

    private void addElderButton(String elderName, String presId) {

        Button btn = new Button(this);
        btn.setText(elderName);
        btn.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 25);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            Intent i = new Intent(MedicationTracker.this, PrescriptionDetailsActivity.class);
            i.putExtra("prescriptionId", presId);
            startActivity(i);
        });

        medicationsContainer.addView(btn);
    }
}
