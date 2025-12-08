package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MedNurseRunningNote extends AppCompatActivity {

    ImageView imgBack;
    EditText editRunningNote;
    Button btnSendGuardian;

    DatabaseReference reference;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_c_g_nurses_runningnotes);

        imgBack = findViewById(R.id.imgBack);
        editRunningNote = findViewById(R.id.editRunningNote);
        btnSendGuardian = findViewById(R.id.btnSendGuardian);

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("RunningNotes");

        imgBack.setOnClickListener(v -> {
            startActivity(new Intent(MedNurseRunningNote.this, MedNurseHomepage.class));
            finish();
        });

        btnSendGuardian.setOnClickListener(v -> saveRunningNote());
    }

    private void saveRunningNote() {
        String note = editRunningNote.getText().toString().trim();
        String nurseId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "Unknown";

        if (TextUtils.isEmpty(note)) {
            editRunningNote.setError("Please enter a note");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String key = reference.push().getKey();

        if (key == null) return;

        RunningNoteModel model = new RunningNoteModel(note, nurseId, timestamp);

        reference.child(key).setValue(model)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(MedNurseRunningNote.this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                    editRunningNote.setText(""); // clear input
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MedNurseRunningNote.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
