package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NurseProfile extends AppCompatActivity {

    private static final String TAG = "NurseProfile";

    // UI elements
    private TextView txtFullName, txtEmail, txtRole, txtContact, txtAge;
    private Button btnLogout;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    BottomNavigationView bottomNav;

    // --- NESTED DATA MODEL CLASS ---
    // This class maps directly to the data fields under the Nurse ID in Firebase
    public static class Nurse {
        public String age;
        public String contactNumber;
        public String email;
        public String firstName;
        public String lastName;
        public String role;

        // Required empty public constructor for Firebase DataSnapshot.getValue()
        public Nurse() {}
    }
    // -------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_profile); // Requires activity_nurse_profile.xml

        // System bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI
        txtFullName = findViewById(R.id.txtFullName);
        txtEmail = findViewById(R.id.txtEmail);
        txtRole = findViewById(R.id.txtRole);
        txtContact = findViewById(R.id.txtContact);
        txtAge = findViewById(R.id.txtAge);
        btnLogout = findViewById(R.id.btnLogout);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        // Database reference path: "Nurse/{uid}"
        databaseRef = FirebaseDatabase.getInstance()
                .getReference("Nurse")
                .child(currentUser.getUid());

        loadUserProfile();
        setupLogout();

        bottomNav = findViewById(R.id.bottomNav);
        // Set the highlighted/selected item to profile
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                return true; // Already home
//            } else if (id == R.id.nav_medLog) {
//                startActivity(new Intent(NurseProfile.this, MedNursePrescription.class));
//                return true;
//            } else if (id == R.id.nav_elderlyStatLog) {
//                startActivity(new Intent(NurseProfile.this, ElderList.class));
//                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(NurseProfile.this, MedNurseHomepage.class));
                return true;
            }
            return false;
        });
    }

    private void loadUserProfile() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Map the snapshot data using the nested Nurse class
                    Nurse nurse = snapshot.getValue(Nurse.class);
                    if (nurse != null) {
                        displayProfileData(nurse);
                    }
                } else {
                    Log.w(TAG, "Profile data not found for UID: " + currentUser.getUid());
                    Toast.makeText(NurseProfile.this, "Profile data not found in database.", Toast.LENGTH_LONG).show();
                    // Display email from Auth as fallback
                    txtEmail.setText("Email: " + currentUser.getEmail());
                    txtFullName.setText("User Data Missing");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile", error.toException());
                Toast.makeText(NurseProfile.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayProfileData(Nurse nurse) {
        String fullName = nurse.firstName + " " + nurse.lastName;
        txtFullName.setText(fullName);
        txtEmail.setText("Email: " + nurse.email);
        txtRole.setText("Role: " + nurse.role);
        txtContact.setText("Contact: " + nurse.contactNumber);
        txtAge.setText("Age: " + nurse.age);
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(NurseProfile.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        // IMPORTANT: Replace LoginActivity.class with the actual class name of your login screen
        Intent intent = new Intent(NurseProfile.this, MedNurseLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish(); // Finish the current activity
    }
}