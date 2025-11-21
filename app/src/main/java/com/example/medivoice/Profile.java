package com.example.medivoice;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Profile extends AppCompatActivity {

    private TextView fullnameText, addressText, contactText, emailText;
    private TextView guardianNameText, guardianContactText, guardianEmailText, guardianAddressText;
    private Button circleButton;
    private Button btnEditProfile, btnAddContacts;
    private BottomNavigationView bottomNavigationView;

    private ActivityResultLauncher<String> pickImageLauncher;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private DatabaseReference guardiansRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

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
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnAddContacts = findViewById(R.id.btnAddContacts);

        guardianNameText = findViewById(R.id.guardianNameText);
        guardianContactText = findViewById(R.id.guardianContactText);
        guardianEmailText = findViewById(R.id.guardianEmailText);
        guardianAddressText = findViewById(R.id.guardianAddressText);

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        userRef = db.getReference("Users").child(uid);
        guardiansRef = db.getReference("Guardians");
        storageRef = FirebaseStorage.getInstance().getReference("profileImages").child(uid + ".jpg");

        initImagePicker();

        circleButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        loadUserData();
        loadProfileImage();
        loadGuardianData();

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_profile) {
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(getApplicationContext(), Logs.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_home) {
                    startActivity(new Intent(getApplicationContext(), HomePage.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditProfile.class);
            startActivity(intent);
        });

        btnAddContacts.setOnClickListener(v -> {
            Intent contactIntent = new Intent(Profile.this, EmergencyContact.class);
            startActivity(contactIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadUserData();
            loadProfileImage();
            loadGuardianData();
        }
    }

    private void initImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadImageToFirebase(uri);
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        UploadTask uploadTask = storageRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String imageUrl = downloadUri.toString();

                    userRef.child("profileImageUrl").setValue(imageUrl)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(Profile.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                setCircleButtonImage(imageUrl);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Profile.this, "Failed to save image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(Profile.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
        ).addOnFailureListener(e ->
                Toast.makeText(Profile.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void setCircleButtonImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                        circleButton.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                    }
                });
    }

    private void loadProfileImage() {
        userRef.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUrl = snapshot.getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    setCircleButtonImage(imageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to load profile image: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(Profile.this, "User data not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                String contactNumber = snapshot.child("contactNumber").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                if (firstName == null) firstName = "";
                if (lastName == null) lastName = "";
                if (address == null) address = "";
                if (contactNumber == null) contactNumber = "";
                if (email == null) email = "";

                String fullName = (firstName + " " + lastName).trim();

                fullnameText.setText("Full Name: " + fullName);
                addressText.setText("Address: " + address);
                contactText.setText("Contact: " + contactNumber);
                emailText.setText("Email: " + email);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Profile.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGuardianData() {
        if (currentUser == null) return;

        userRef.child("guardians").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    guardianNameText.setText("Name: None connected");
                    guardianContactText.setText("Contact: -");
                    guardianEmailText.setText("Email: -");
                    guardianAddressText.setText("Address: -");
                    return;
                }

                String guardianId = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    guardianId = child.getKey();
                    break;
                }

                if (guardianId == null) {
                    guardianNameText.setText("Name: None connected");
                    guardianContactText.setText("Contact: -");
                    guardianEmailText.setText("Email: -");
                    guardianAddressText.setText("Address: -");
                    return;
                }

                guardiansRef.child(guardianId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot guardianSnap) {
                        if (!guardianSnap.exists()) {
                            guardianNameText.setText("Name: Guardian not found");
                            guardianContactText.setText("Contact: -");
                            guardianEmailText.setText("Email: -");
                            guardianAddressText.setText("Address: -");
                            return;
                        }

                        String gFirst = guardianSnap.child("firstName").getValue(String.class);
                        String gLast = guardianSnap.child("lastName").getValue(String.class);
                        String gContact = guardianSnap.child("contactNumber").getValue(String.class);
                        String gEmail = guardianSnap.child("email").getValue(String.class);
                        String gAddress = guardianSnap.child("address").getValue(String.class);

                        if (gFirst == null) gFirst = "";
                        if (gLast == null) gLast = "";
                        if (gContact == null) gContact = "";
                        if (gEmail == null) gEmail = "";
                        if (gAddress == null) gAddress = "";

                        String gFullName = (gFirst + " " + gLast).trim();

                        guardianNameText.setText("Name: " + gFullName);
                        guardianContactText.setText("Contact: " + gContact);
                        guardianEmailText.setText("Email: " + gEmail);
                        guardianAddressText.setText("Address: " + gAddress);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Profile.this, "Failed to load guardian: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to load guardian link: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
