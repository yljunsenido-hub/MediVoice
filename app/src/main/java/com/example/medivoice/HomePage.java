package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class HomePage extends AppCompatActivity {

    Button generateButton,prescriptionButton,voiceButton;
    TextView codeView;
    DatabaseReference usersRef;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        generateButton = findViewById(R.id.generateButton);
        prescriptionButton = findViewById(R.id.prescriptionButton);
        voiceButton = findViewById(R.id.voiceButton);
        codeView = findViewById(R.id.codeView);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        generateButton.setOnClickListener(v -> generateConnectionCode());

        prescriptionButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, PrescriptionScanner.class);
            startActivity(intent);
        });

        voiceButton.setOnClickListener(v -> {
            Intent voiceIntent = new Intent(HomePage.this, TextToSpeech.class);
            startActivity(voiceIntent);
        });

    }

    private void generateConnectionCode() {
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        HashMap<String, Object> codeData = new HashMap<>();
        codeData.put("code", code);
        codeData.put("used", false);
        codeData.put("createdAt", System.currentTimeMillis());

        usersRef.child(userId).child("connectCode").setValue(codeData)
                .addOnSuccessListener(aVoid -> {
                    codeView.setText("Your Code: " + code);
                    Toast.makeText(this, "Code generated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
