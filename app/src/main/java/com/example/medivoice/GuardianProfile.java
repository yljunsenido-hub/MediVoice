package com.example.medivoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

public class GuardianProfile extends AppCompatActivity {

    private TextView fullnameText, addressText, contactText, emailText;
    private Button circleButton;
    private Button btnEditProfile;
    private BottomNavigationView bottomNavigationView;

    private FirebaseUser currentUser;
    private DatabaseReference guardianRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guardian_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullnameText = findViewById(R.id.fullnameText);
        addressText = findViewById(R.id.addressText);
        contactText = findViewById(R.id.contactText);
        emailText = findViewById(R.id.emailText);
        circleButton = findViewById(R.id.circleButton);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // bottom nav – choose what tab represents guardian/profile
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // THIS IS YOUR GUARDIAN DATABASE NODE:
        // Guardians/{uid}
        guardianRef = db.getReference("Guardians").child(uid);

        loadGuardianData();

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_profile) {
                    // If this nav is for guardian, you can stay here
                    // or move to normal Profile
                    startActivity(new Intent(getApplicationContext(), GuardianProfile.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(getApplicationContext(), GuardianLogs.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_home) {
                    startActivity(new Intent(getApplicationContext(), GuardianHomePage.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });

        btnEditProfile.setOnClickListener(v -> {
            // If you have a separate edit screen for guardians, change this
            Intent intent = new Intent(GuardianProfile.this, EditProfile.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadGuardianData();
        }
    }

    private void loadGuardianData() {
        guardianRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(GuardianProfile.this, "Guardian data not found", Toast.LENGTH_SHORT).show();
                    fullnameText.setText("Fullname: -");
                    addressText.setText("Address: -");
                    contactText.setText("Contact: -");
                    emailText.setText("Email: -");
                    return;
                }

                String firstName = snapshot.child("firstName").getValue(String.class);
                String middleName = snapshot.child("middleName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                String contactNumber = snapshot.child("contactNumber").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                if (firstName == null) firstName = "";
                if (middleName == null) middleName = "";
                if (lastName == null) lastName = "";
                if (address == null) address = "";
                if (contactNumber == null) contactNumber = "";
                if (email == null) email = "";

                String fullName = (firstName + " " + middleName + " " + lastName).trim().replaceAll(" +", " ");

                fullnameText.setText("Fullname: " + fullName);
                addressText.setText("Address: " + address);
                contactText.setText("Contact: " + contactNumber);
                emailText.setText("Email: " + email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GuardianProfile.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
