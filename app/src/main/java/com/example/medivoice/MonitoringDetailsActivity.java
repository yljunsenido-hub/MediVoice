package com.example.medivoice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MonitoringDetailsActivity extends AppCompatActivity {

    TextView txtElderName;
    EditText edtBloodPressure, edtTemperature, edtPulseRate, edtRespiratoryRate,
            edtOxygen, edtBloodSugar, edtUrineOutput, edtStool;
    Button btnSaveVitals, btnSaveOutput;

    String elderId, nurseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_details);

        elderId = getIntent().getStringExtra("elderId");
        String elderName = getIntent().getStringExtra("elderName");
        nurseId = getIntent().getStringExtra("nurseId"); // nurse assigned to this elder

        txtElderName = findViewById(R.id.txtElderName);
        edtBloodPressure = findViewById(R.id.edtBloodPressure);
        edtTemperature = findViewById(R.id.edtTemperature);
        edtPulseRate = findViewById(R.id.edtPulseRate);
        edtRespiratoryRate = findViewById(R.id.edtRespiratoryRate);
        edtOxygen = findViewById(R.id.edtOxygen);
        edtBloodSugar = findViewById(R.id.edtBloodSugar);
        edtUrineOutput = findViewById(R.id.edtUrineOutput);
        edtStool = findViewById(R.id.edtStool);

        btnSaveVitals = findViewById(R.id.btnSaveVitals);
        btnSaveOutput = findViewById(R.id.btnSaveOutput);

        txtElderName.setText(elderName);

        btnSaveVitals.setOnClickListener(v -> saveVitalsToExistingNurse());
        btnSaveOutput.setOnClickListener(v -> saveOutputToExistingNurse());
    }

    private void saveVitalsToExistingNurse() {
        if (nurseId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Nurse")          // use existing Nurse node
                .child(nurseId)
                .child(elderId)
                .child("VitalSigns");

        String recordId = ref.push().getKey();

        HashMap<String, Object> data = new HashMap<>();
        data.put("bloodPressure", edtBloodPressure.getText().toString());
        data.put("temperature", edtTemperature.getText().toString());
        data.put("pulseRate", edtPulseRate.getText().toString());
        data.put("respiratoryRate", edtRespiratoryRate.getText().toString());
        data.put("oxygenSaturation", edtOxygen.getText().toString());
        data.put("bloodSugar", edtBloodSugar.getText().toString());
        data.put("timestamp", System.currentTimeMillis());

        ref.child(recordId).setValue(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Vital signs saved under Nurse node!", Toast.LENGTH_SHORT).show());
    }

    private void saveOutputToExistingNurse() {
        if (nurseId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Nurse")          // use existing Nurse node
                .child(nurseId)
                .child(elderId)
                .child("OutputMonitoring");

        String recordId = ref.push().getKey();

        HashMap<String, Object> data = new HashMap<>();
        data.put("urineOutput", edtUrineOutput.getText().toString());
        data.put("stool", edtStool.getText().toString());
        data.put("timestamp", System.currentTimeMillis());

        ref.child(recordId).setValue(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Output monitoring saved under Nurse node!", Toast.LENGTH_SHORT).show());
    }
}
