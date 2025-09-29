package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EnterCodePage extends AppCompatActivity {

    EditText codeInput;
    Button btnSubmitCode;
    String guardianId;
    DatabaseReference usersRef, guardiansRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter_code_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        codeInput = findViewById(R.id.codeInput);
        btnSubmitCode = findViewById(R.id.btnSubmitCode);

        guardianId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        guardiansRef = FirebaseDatabase.getInstance().getReference("Guardians");

        btnSubmitCode.setOnClickListener(v -> {
            String inputCode = codeInput.getText().toString().trim();
            if (TextUtils.isEmpty(inputCode)) {
                codeInput.setError("Enter the code");
            } else {
                verifyAndConnect(inputCode);
            }
        });
    }

    private void verifyAndConnect(String codeFromGuardian) {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean found = false;
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    if (userSnapshot.hasChild("connectCode")) {
                        String userCode = userSnapshot.child("connectCode/code").getValue(String.class);
                        Boolean used = userSnapshot.child("connectCode/used").getValue(Boolean.class);

                        if (userCode != null && userCode.equals(codeFromGuardian) && Boolean.FALSE.equals(used)) {
                            String userId = userSnapshot.getKey();
                            connectGuardianToUser(userId);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    Toast.makeText(this, "Invalid or already used code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectGuardianToUser(String userId) {
        // Add to user's guardians list
        usersRef.child(userId).child("guardians").child(guardianId).setValue(true);

        // Add to guardian's users list
        guardiansRef.child(guardianId).child("users").child(userId).setValue(true);

        // Mark code as used
        usersRef.child(userId).child("connectCode/used").setValue(true);

        Toast.makeText(this, "Connected Successfully!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, GuardianHomePage.class));
        finish();
    }
}