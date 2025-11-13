package com.example.medivoice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TextToSpeech extends AppCompatActivity {

    Button micButton, saveButton,backButton;
    TextView outputView;
    EditText recordName;

    private static final int SPEECH_REQUEST_CODE = 100;

    FirebaseAuth mAuth;
    DatabaseReference userSpeechRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_to_speech);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ask for mic permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 300);
        }

        micButton = findViewById(R.id.micButton);
        outputView = findViewById(R.id.outputView);
        saveButton = findViewById(R.id.saveButton);
        recordName = findViewById(R.id.recordName);

        // Firebase Saving Record
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TextToSpeech.this, HomePage.class);
            startActivity(intent);
        });

        if (currentUser != null) {
            userSpeechRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("SpeechToText");
        }

        micButton.setOnClickListener(v -> startSpeechToText());

        saveButton.setOnClickListener(v -> {
            String name = recordName.getText().toString().trim();
            String text = outputView.getText().toString().trim();
            String date = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a record name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (text.isEmpty()) {
                Toast.makeText(this, "No text to save!", Toast.LENGTH_SHORT).show();
                return;
            }

            String noteId = userSpeechRef.push().getKey();

            // Create an object with Name, Text, and Date
            SpeechRecord record = new SpeechRecord(name, text, date);

            userSpeechRef.child(noteId).setValue(record)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...");
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            outputView.setText(result.get(0));
        }
    }
}
