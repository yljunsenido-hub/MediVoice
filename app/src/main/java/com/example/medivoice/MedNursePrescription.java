package com.example.medivoice;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MedNursePrescription extends AppCompatActivity {

    Spinner spinnerElderName, spinnerSendTo, spinnerObsElderName, spinnerObsSendTo;
    EditText etMedicationName, etDosage, etSchedule, etToBeMonitored, etTime;
    Button btnSendTo, btnObsSendTo, btnSetAlarm;

    ImageView imgBack;

    FirebaseAuth mAuth;
    DatabaseReference eldersRef, caregiversRef, nurseRef, rootRef;

    ArrayList<String> elderNames = new ArrayList<>();
    ArrayList<String> elderIds = new ArrayList<>();

    ArrayList<String> caregiverNames = new ArrayList<>();
    ArrayList<String> caregiverIds = new ArrayList<>();

    String nurseId;
    String nurseName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_nurse_prescription);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


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
        etTime = findViewById(R.id.etTime); // NEW

        btnSendTo = findViewById(R.id.btnSendTo);
        btnObsSendTo = findViewById(R.id.btnObsSendTo);
        btnSetAlarm = findViewById(R.id.btnSetAlarm);

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

        btnSendTo.setOnClickListener(v -> sendPrescription());
        btnObsSendTo.setOnClickListener(v -> sendObservation());

        btnSetAlarm.setOnClickListener(v -> showTimePicker());
    }

    void loadNurseName() {
        nurseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String first = "";
                    String last = "";
                    Object fObj = snapshot.child("firstName").getValue();
                    Object lObj = snapshot.child("lastName").getValue();
                    if (fObj != null) first = fObj.toString();
                    if (lObj != null) last = lObj.toString();
                    nurseName = (first + " " + last).trim();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
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
                    Object nameObj = child.child("name").getValue();
                    if (nameObj != null) {
                        name = nameObj.toString();
                    } else if (child.child("elderName").getValue() != null) {
                        name = child.child("elderName").getValue().toString();
                    }
                    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(name)) {
                        elderIds.add(id);
                        elderNames.add(name);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MedNursePrescription.this, android.R.layout.simple_spinner_item, elderNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerElderName.setAdapter(adapter);
                spinnerObsElderName.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
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
                    String first = "";
                    String last = "";
                    Object fObj = child.child("firstName").getValue();
                    Object lObj = child.child("lastName").getValue();
                    if (fObj != null) first = fObj.toString();
                    if (lObj != null) last = lObj.toString();
                    String name = (first + " " + last).trim();
                    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(name)) {
                        caregiverIds.add(id);
                        caregiverNames.add(name);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MedNursePrescription.this, android.R.layout.simple_spinner_item, caregiverNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSendTo.setAdapter(adapter);
                spinnerObsSendTo.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // NEW: show time picker and display into etTime
    void showTimePicker() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String formatted = formatTime(selectedHour, selectedMinute);
                    etTime.setText(formatted);
                },
                hour,
                minute,
                false
        );
        dialog.show();
    }

    // NEW: format 24h to 12h with AM/PM
    String formatTime(int hourOfDay, int minute) {
        int hour = hourOfDay % 12;
        if (hour == 0) hour = 12;
        String ampm = (hourOfDay < 12) ? "AM" : "PM";
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, ampm);
    }

    void sendPrescription() {
        int elderPos = spinnerElderName.getSelectedItemPosition();
        int caregiverPos = spinnerSendTo.getSelectedItemPosition();

        if (elderPos <= 0 || caregiverPos <= 0) {
            Toast.makeText(this, "Select elder and caregiver", Toast.LENGTH_SHORT).show();
            return;
        }

        String elderId = elderIds.get(elderPos);
        String elderName = elderNames.get(elderPos);

        String caregiverId = caregiverIds.get(caregiverPos);

        String medName = etMedicationName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String schedule = etSchedule.getText().toString().trim();
        String time = etTime.getText().toString().trim(); // NEW

        if (TextUtils.isEmpty(medName) || TextUtils.isEmpty(dosage) || TextUtils.isEmpty(schedule) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Fill all medication fields and time", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = rootRef.child("Caregiver").child(caregiverId).child("Prescriptions").push().getKey();
        if (key == null) {
            Toast.makeText(this, "Error creating record", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("prescriptionId", key);
        data.put("elderId", elderId);
        data.put("elderName", elderName);
        data.put("medicationName", medName);
        data.put("dosage", dosage);
        data.put("schedule", schedule);
        data.put("time", time); // NEW: saved to Firebase
        data.put("nurseId", nurseId);
        data.put("nurseName", nurseName);
        data.put("timestamp", getTimestamp());

        rootRef.child("Caregiver").child(caregiverId).child("Prescriptions").child(key)
                .setValue(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(MedNursePrescription.this, "Prescription sent", Toast.LENGTH_SHORT).show();
                    etMedicationName.setText("");
                    etDosage.setText("");
                    etSchedule.setText("");
                    etTime.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(MedNursePrescription.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    void sendObservation() {
        int elderPos = spinnerObsElderName.getSelectedItemPosition();
        int caregiverPos = spinnerObsSendTo.getSelectedItemPosition();

        if (elderPos <= 0 || caregiverPos <= 0) {
            Toast.makeText(this, "Select elder and caregiver", Toast.LENGTH_SHORT).show();
            return;
        }

        String elderId = elderIds.get(elderPos);
        String elderName = elderNames.get(elderPos);

        String caregiverId = caregiverIds.get(caregiverPos);

        String toBeMonitored = etToBeMonitored.getText().toString().trim();

        if (TextUtils.isEmpty(toBeMonitored)) {
            Toast.makeText(this, "Fill monitoring details", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = rootRef.child("Caregiver").child(caregiverId).child("Observations").push().getKey();
        if (key == null) {
            Toast.makeText(this, "Error creating record", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("observationId", key);
        data.put("elderId", elderId);
        data.put("elderName", elderName);
        data.put("monitoringSchedule", toBeMonitored);
        data.put("nurseId", nurseId);
        data.put("nurseName", nurseName);
        data.put("timestamp", getTimestamp());

        rootRef.child("Caregiver").child(caregiverId).child("Observations").child(key)
                .setValue(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(MedNursePrescription.this, "Observation sent", Toast.LENGTH_SHORT).show();
                    etToBeMonitored.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(MedNursePrescription.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
