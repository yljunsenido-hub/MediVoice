package com.example.medivoice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.UUID;

public class CodeGenerator extends AppCompatActivity {

    Button generateButton;


    DatabaseReference usersRef;
    TextView codeView;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_code_generator);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        generateButton = findViewById(R.id.generateButton);
        codeView = findViewById(R.id.codeView);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        //copy Button
        Button copyButton = findViewById(R.id.copyButton);

        copyButton.setOnClickListener(v -> copyCodeToClipboard());

        // Check if a code already exists in Firebase
        usersRef.child(userId).child("connectCode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Code already exists, fetch it and display
                    String code = snapshot.child("code").getValue(String.class);
                    codeView.setText(code);
                    generateButton.setEnabled(false); // disable generate button
                    generateButton.setAlpha(0.5f); // visually indicate disabled
                } else {
                    // No code exists, allow generation
                    generateButton.setOnClickListener(v -> generateConnectionCode());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CodeGenerator.this, "Failed to fetch code: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
                    codeView.setText(code);
                    Toast.makeText(this, "Code generated!", Toast.LENGTH_SHORT).show();
                    generateButton.setEnabled(false);
                    generateButton.setAlpha(0.5f);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void copyCodeToClipboard() {
        String code = codeView.getText().toString().trim();
        if (!code.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Connection Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Code copied to clipboard!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No code to copy!", Toast.LENGTH_SHORT).show();
        }
    }
}
