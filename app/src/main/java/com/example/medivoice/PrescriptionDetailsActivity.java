package com.example.medivoice;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class PrescriptionDetailsActivity extends AppCompatActivity {

    private LinearLayout detailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_details);

        detailsContainer = findViewById(R.id.detailsContainer);

        String presId = getIntent().getStringExtra("prescriptionId");

        loadDetails(presId);
    }

    private void loadDetails(String presId) {
        String caregiverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Caregiver")
                .child(caregiverId)
                .child("Prescriptions")
                .child(presId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) return;

                    String elder = snapshot.child("elderName").getValue(String.class);
                    String med = snapshot.child("medicationName").getValue(String.class);
                    String dosage = snapshot.child("dosage").getValue(String.class);
                    String schedule = snapshot.child("schedule").getValue(String.class);
                    String time = snapshot.child("time").getValue(String.class);
                    String nurse = snapshot.child("nurseName").getValue(String.class);

                    addSingleCard(elder, med, dosage, schedule, time, nurse);
                });
    }

    // ONE BIG CARD WITH ALL DETAILS
    private void addSingleCard(String elder, String med, String dosage,
                               String schedule, String time, String nurse) {

        CardView card = new CardView(this);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 25);

        card.setLayoutParams(cardParams);
        card.setCardElevation(12f);
        card.setRadius(18f);
        card.setUseCompatPadding(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        layout.addView(makeRow("Elder Name", elder));
        layout.addView(makeRow("Medicine Name", med));
        layout.addView(makeRow("Dosage", dosage));
        layout.addView(makeRow("Schedule", schedule));
        layout.addView(makeRow("Time", time));
        layout.addView(makeRow("Assigned Nurse", nurse));

        card.addView(layout);
        detailsContainer.addView(card);
    }

    private LinearLayout makeRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 10, 0, 10);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.BLACK);
        tvLabel.setTextSize(16);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextColor(Color.DKGRAY);
        tvValue.setTextSize(20);
        tvValue.setPadding(0, 6, 0, 0);

        row.addView(tvLabel);
        row.addView(tvValue);

        return row;
    }
}
