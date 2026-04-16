package com.example.medivoice;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MedNursePrescription extends AppCompatActivity {

    Spinner spinnerElderName, spinnerSendTo, spinnerObsElderName, spinnerObsSendTo;
    EditText etMedicationName, etDosage, etSchedule, etToBeMonitored, etTime;

    ImageView imgBack;

    FirebaseAuth mAuth;
    DatabaseReference eldersRef, caregiversRef, nurseRef, rootRef;

    ArrayList<String> elderNames = new ArrayList<>();
    ArrayList<String> elderIds = new ArrayList<>();

    ArrayList<String> caregiverNames = new ArrayList<>();
    ArrayList<String> caregiverIds = new ArrayList<>();

    String nurseId;
    String nurseName = "";

    int selectedHour = -1;
    int selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_nurse_prescription);

        imgBack = findViewById(R.id.imgBack);

        imgBack.setOnClickListener(v -> {
            startActivity(new Intent(MedNursePrescription.this, MedNurseHomepage.class));
            finish();
        });

        spinnerElderName = findViewById(R.id.spinnerElderName);
        spinnerSendTo = findViewById(R.id.spinnerSendTo);
        spinnerObsElderName = findViewById(R.id.spinnerObsElderName);
        spinnerObsSendTo = findViewById(R.id.spinnerObsSendTo);

        etMedicationName = findViewById(R.id.etMedicationName);
        etDosage = findViewById(R.id.etDosage);
        etSchedule = findViewById(R.id.etSchedule);
        etToBeMonitored = findViewById(R.id.etToBeMonitored);
        etTime = findViewById(R.id.etTime);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        rootRef = FirebaseDatabase.getInstance().getReference();
        eldersRef = rootRef.child("Elders");
        caregiversRef = rootRef.child("Caregiver");

        if (user != null) {
            nurseId = user.getUid();
            nurseRef = rootRef.child("Nurse").child(nurseId);
            loadNurseName();
        }

        loadElders();
        loadCaregivers();

        // TIME CLICK → SHOW PICKER
        etTime.setOnClickListener(v -> showTimePicker());

        findViewById(R.id.btnSendTo).setOnClickListener(v -> sendPrescription());
        findViewById(R.id.btnObsSendTo).setOnClickListener(v -> sendObservation());
    }

    void loadNurseName() {
        nurseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String first = String.valueOf(snapshot.child("firstName").getValue());
                    String last = String.valueOf(snapshot.child("lastName").getValue());
                    nurseName = (first + " " + last).trim();
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    void loadElders() {
        elderNames.clear();
        elderIds.clear();
        elderNames.add("Select Elder");
        elderIds.add("");

        eldersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = child.getKey();
                    String name = "";

                    if (child.child("name").getValue() != null)
                        name = child.child("name").getValue().toString();
                    else if (child.child("elderName").getValue() != null)
                        name = child.child("elderName").getValue().toString();

                    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(name)) {
                        elderIds.add(id);
                        elderNames.add(name);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MedNursePrescription.this,
                        android.R.layout.simple_spinner_item, elderNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerElderName.setAdapter(adapter);
                spinnerObsElderName.setAdapter(adapter);
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    void loadCaregivers() {
        caregiverNames.clear();
        caregiverIds.clear();
        caregiverNames.add("Select Caregiver");
        caregiverIds.add("");

        caregiversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = child.getKey();
                    String first = String.valueOf(child.child("firstName").getValue());
                    String last = String.valueOf(child.child("lastName").getValue());
                    String name = (first + " " + last).trim();

                    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(name)) {
                        caregiverIds.add(id);
                        caregiverNames.add(name);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MedNursePrescription.this,
                        android.R.layout.simple_spinner_item, caregiverNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerSendTo.setAdapter(adapter);
                spinnerObsSendTo.setAdapter(adapter);
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    void showTimePicker() {
        Calendar c = Calendar.getInstance();

        new TimePickerDialog(this, (view, hour, minute) -> {

            selectedHour = hour;
            selectedMinute = minute;

            int h = hour % 12;
            if (h == 0) h = 12;

            String ampm = hour < 12 ? "AM" : "PM";

            etTime.setText(String.format(Locale.getDefault(),
                    "%02d:%02d %s", h, minute, ampm));

        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

    void sendPrescription() {
        int elderPos = spinnerElderName.getSelectedItemPosition();
        int caregiverPos = spinnerSendTo.getSelectedItemPosition();

        if (elderPos <= 0 || caregiverPos <= 0) {
            Toast.makeText(this, "Select elder and caregiver", Toast.LENGTH_SHORT).show();
            return;
        }

        String medName = etMedicationName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String schedule = etSchedule.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (TextUtils.isEmpty(medName) || TextUtils.isEmpty(dosage)
                || TextUtils.isEmpty(schedule) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;

        }

        String caregiverId = caregiverIds.get(caregiverPos);
        String key = rootRef.child("Caregiver").child(caregiverId)
                .child("Prescriptions").push().getKey();

        HashMap<String, Object> data = new HashMap<>();
        data.put("prescriptionId", key);
        data.put("elderName", elderNames.get(elderPos));
        data.put("medicationName", medName);
        data.put("dosage", dosage);
        data.put("schedule", schedule);
        data.put("time", time);
        data.put("nurseName", nurseName);
        data.put("timestamp", getTimestamp());
        data.put("status", "pending");

        rootRef.child("Caregiver").child(caregiverId)
                .child("Prescriptions").child(key)
                .setValue(data)
                .addOnSuccessListener(unused -> {
                    showPrescriptionNotification(medName);

                    scheduleMedicationAlarm(medName);

                    etMedicationName.setText("");
                    etDosage.setText("");
                    etSchedule.setText("");
                    etTime.setText("");
                });
    }

    void sendObservation() {
        int elderPos = spinnerObsElderName.getSelectedItemPosition();
        int caregiverPos = spinnerObsSendTo.getSelectedItemPosition();

        if (elderPos <= 0 || caregiverPos <= 0) {
            Toast.makeText(this, "Select elder and caregiver", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etToBeMonitored.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Fill monitoring details", Toast.LENGTH_SHORT).show();
            return;
        }

        String caregiverId = caregiverIds.get(caregiverPos);
        String key = rootRef.child("Caregiver").child(caregiverId)
                .child("Observations").push().getKey();

        HashMap<String, Object> data = new HashMap<>();
        data.put("observationId", key);
        data.put("elderName", elderNames.get(elderPos));
        data.put("monitoringSchedule", text);
        data.put("nurseName", nurseName);
        data.put("timestamp", getTimestamp());

        rootRef.child("Caregiver").child(caregiverId)
                .child("Observations").child(key)
                .setValue(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Observation sent", Toast.LENGTH_SHORT).show();
                    etToBeMonitored.setText("");
                });
    }
    void scheduleMedicationAlarm(String medName) {

        if (selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(this, "Please select time", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar now = Calendar.getInstance();
        Calendar alarmCal = Calendar.getInstance();

        alarmCal.set(Calendar.HOUR_OF_DAY, selectedHour);
        alarmCal.set(Calendar.MINUTE, selectedMinute);
        alarmCal.set(Calendar.SECOND, 0);

        if (alarmCal.before(now)) {
            alarmCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medName", medName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager == null) {
            Toast.makeText(this, "AlarmManager not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmCal.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        alarmCal.getTimeInMillis(),
                        pendingIntent
                );
            }
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmCal.getTimeInMillis(),
                    pendingIntent
            );
        }

        Toast.makeText(this, "Alarm scheduled", Toast.LENGTH_SHORT).show();
    }
    void showPrescriptionNotification(String medName) {
        String channelId = "prescription_channel";

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Prescription Notifications",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Prescription Sent")
                .setContentText("Medication: " + medName)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
    }
}