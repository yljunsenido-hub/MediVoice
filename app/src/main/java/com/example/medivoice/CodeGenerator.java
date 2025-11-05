package com.example.medivoice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

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

        generateButton.setOnClickListener(v -> generateConnectionCode());
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
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}