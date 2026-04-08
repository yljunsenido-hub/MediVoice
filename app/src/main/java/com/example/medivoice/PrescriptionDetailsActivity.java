package com.example.medivoice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class PrescriptionDetailsActivity extends AppCompatActivity {

    private LinearLayout detailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_details);

        detailsContainer = findViewById(R.id.detailsContainer);

        android.widget.ImageView btnBack = findViewById(R.id.imgBack);
        btnBack.setOnClickListener(v -> finish());

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

                    addSingleCard(presId, elder, med, dosage, schedule, time, nurse);

                    scheduleAlarm(time, med);
                });
    }

    private void addSingleCard(String presId, String elder, String med, String dosage,
                               String schedule, String time, String nurse) {

        CardView card = new CardView(this);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 30);

        card.setLayoutParams(cardParams);
        card.setCardElevation(10f);
        card.setRadius(20f);
        card.setUseCompatPadding(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        layout.addView(makeRow("Elder Name", elder));
        layout.addView(makeRow("Medicine Name", med));
        layout.addView(makeRow("Dosage", dosage));
        layout.addView(makeRow("Schedule", schedule));
        layout.addView(makeRow("Time", time));
        layout.addView(makeRow("Assigned Nurse", nurse));

        android.widget.Button btnYes = new android.widget.Button(this);
        btnYes.setText("Confirm Medication");
        btnYes.setBackgroundResource(R.drawable.bg_med_button);
        btnYes.setTextColor(Color.WHITE);
        btnYes.setTextSize(16);
        btnYes.setPadding(0, 20, 0, 20);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 30, 0, 0);
        btnYes.setLayoutParams(btnParams);

        layout.addView(btnYes);

        btnYes.setOnClickListener(v -> {

            String caregiverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference()
                    .child("Caregiver")
                    .child(caregiverId)
                    .child("Prescriptions")
                    .child(presId)
                    .child("status")
                    .setValue("confirmed");

            FirebaseDatabase.getInstance().getReference()
                    .child("MedicationLog")
                    .child(presId)
                    .child("status")
                    .setValue("confirmed");

            btnYes.setText("Confirmed");
            btnYes.setEnabled(false);
            btnYes.setAlpha(0.7f);
        });

        card.addView(layout);
        detailsContainer.addView(card);
    }

    private LinearLayout makeRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 15, 0, 15);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.BLACK);
        tvLabel.setTextSize(18);
        tvLabel.setTypeface(null, Typeface.BOLD);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextColor(Color.DKGRAY);
        tvValue.setTextSize(16);
        tvValue.setPadding(0, 8, 0, 0);

        row.addView(tvLabel);
        row.addView(tvValue);

        return row;
    }

    private void scheduleAlarm(String time, String medName) {
        try {
            String[] parts = time.split(" ");
            String hourMin = parts[0];
            String ampm = parts[1];

            String[] hm = hourMin.split(":");
            int hour = Integer.parseInt(hm[0]);
            int minute = Integer.parseInt(hm[1]);

            if (ampm.equalsIgnoreCase("PM") && hour != 12) hour += 12;
            if (ampm.equalsIgnoreCase("AM") && hour == 12) hour = 0;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("medName", medName);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 1001, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}