package com.example.medivoice;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HomePage extends AppCompatActivity {

    Button generateButton, prescriptionButton, voiceButton, textButton, logsButton, addContactButton;
    TextView codeView;
    DatabaseReference usersRef;
    String userId;
    private FloatingActionButton fabMain;
    private FrameLayout fabContainer;
    private boolean isExpanded = false;
    private final List<View> spawnedViews = new ArrayList<>();
    private static final int ANIM_DURATION = 360;
    private static final int COLLAPSE_DURATION = 250;
    private DatabaseReference databaseRef;

    private FirebaseAuth auth;
    private ActivityResultLauncher<String> requestCallPermissionLauncher;

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
        textButton = findViewById(R.id.textButton);
        logsButton = findViewById(R.id.logsButton);
        codeView = findViewById(R.id.codeView);
        addContactButton = findViewById(R.id.addContactButton);

        fabMain = findViewById(R.id.fabMain);
        fabContainer = findViewById(R.id.fabContainer);

        auth = FirebaseAuth.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        databaseRef = FirebaseDatabase.getInstance().getReference();

        requestCallPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {});

        fabMain.setOnClickListener(v -> {
            if (!isExpanded) {
                loadContactsAndExpand();
            } else {
                collapseMenu();
            }
        });

        fabContainer.setOnClickListener(v -> {
            if (isExpanded) collapseMenu();
        });

        generateButton.setOnClickListener(v -> generateConnectionCode());
        prescriptionButton.setOnClickListener(v -> startActivity(new Intent(HomePage.this, RecordPrescriptionScanner.class)));
        voiceButton.setOnClickListener(v -> startActivity(new Intent(HomePage.this, RecordSpeechToText.class)));
        textButton.setOnClickListener(v -> startActivity(new Intent(HomePage.this, TextFeaturePage.class)));
        logsButton.setOnClickListener(v -> startActivity(new Intent(HomePage.this, Logs.class)));

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            databaseRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("EmergencyContacts");
        }

        addContactButton.setOnClickListener(v -> {
            EditText nameInput = findViewById(R.id.nameInput);
            EditText contactInput = findViewById(R.id.contactInput);

            String name = nameInput.getText().toString().trim();
            String contact = contactInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a name!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (contact.isEmpty()) {
                Toast.makeText(this, "Please enter a contact number!", Toast.LENGTH_SHORT).show();
                return;
            }

            String noteId = databaseRef.push().getKey();
            if (noteId != null) {
                HashMap<String, Object> data = new HashMap<>();
                data.put("name", name);
                data.put("contact", contact);

                databaseRef.child(noteId).setValue(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Emergency contact added!", Toast.LENGTH_SHORT).show();
                            nameInput.setText("");
                            contactInput.setText("");
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                    codeView.setText("Your Code: " + code);
                    Toast.makeText(this, "Code generated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static class Contact {
        public String name;
        public String number;
        public Contact() {}
        public Contact(String name, String number) {
            this.name = name;
            this.number = number;
        }
    }

    private void loadContactsAndExpand() {
        if (isExpanded) return;
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(HomePage.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
                return;
            }

            DataSnapshot snap = task.getResult();
            List<Contact> contacts = new ArrayList<>();
            for (DataSnapshot child : snap.getChildren()) {
                String name = child.child("name").getValue(String.class);
                String number = child.child("contact").getValue(String.class);
                if (name != null && number != null) {
                    contacts.add(new Contact(name, number));
                }
            }

            if (contacts.isEmpty()) {
                Toast.makeText(HomePage.this, "No emergency contacts found", Toast.LENGTH_SHORT).show();
                return;
            }
            expandFabMenu(contacts);
        }).addOnFailureListener(e ->
                Toast.makeText(HomePage.this, "Error fetching contacts", Toast.LENGTH_SHORT).show());
    }

    private void expandFabMenu(List<Contact> contacts) {
        if (isExpanded) return;
        isExpanded = true;

        int[] fabLoc = new int[2];
        fabMain.getLocationOnScreen(fabLoc);
        int[] containerLoc = new int[2];
        fabContainer.getLocationOnScreen(containerLoc);

        float fabCenterX = fabLoc[0] - containerLoc[0] + fabMain.getWidth() / 2f;
        float fabCenterY = fabLoc[1] - containerLoc[1] + fabMain.getHeight() / 2f;

        final float radiusPx = dpToPx(70f); // reduced for closer placement
        final int totalContacts = contacts.size();
        final int visibleContacts = Math.min(4, totalContacts); // max 4 contacts

        LayoutInflater inflater = LayoutInflater.from(this);

        int itemCount = visibleContacts < totalContacts ? visibleContacts + 1 : visibleContacts; // add "More" if needed

        for (int i = 0; i < itemCount; i++) {
            View item = inflater.inflate(R.layout.item_contact_radial, fabContainer, false);
            LinearLayout circleButton = item.findViewById(R.id.circleButton);
            TextView nameTv = item.findViewById(R.id.contactName);

            String label;
            String phoneNumber = null;
            boolean isMoreButton = false;

            if (i < visibleContacts) {
                Contact contact = contacts.get(i);
                label = contact.name.substring(0, 1).toUpperCase();
                phoneNumber = contact.number;
            } else {
                label = "+";
                isMoreButton = true;
            }

            nameTv.setText(label);

            item.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int w = item.getMeasuredWidth();
            int h = item.getMeasuredHeight();

            item.setX(fabCenterX - w / 2f);
            item.setY(fabCenterY - h / 2f);
            item.setAlpha(0f);
            item.setScaleX(0.1f);
            item.setScaleY(0.1f);

            final boolean finalIsMoreButton = isMoreButton;
            final String finalPhoneNumber = phoneNumber;
            item.setOnClickListener(v -> {
                if (finalIsMoreButton) {
                    Toast.makeText(this, "Show more contacts...", Toast.LENGTH_SHORT).show();
                    // You can open another activity or dialog here later
                } else {
                    attemptCall(finalPhoneNumber);
                }
                collapseMenu();
            });

            fabContainer.addView(item);
            spawnedViews.add(item);

            double startAngle = 100; // upper-right arc
            double sweepAngle = 90;
            double angleDeg = startAngle - (sweepAngle / (itemCount - 1)) * i;
            double angleRad = Math.toRadians(angleDeg + 90);

            float targetX = (float) (radiusPx * Math.cos(angleRad));
            float targetY = (float) (-radiusPx * Math.sin(angleRad));

            float finalX = fabCenterX - w / 2f + targetX;
            float finalY = fabCenterY - h / 2f + targetY;

            ObjectAnimator animX = ObjectAnimator.ofFloat(item, "x", item.getX(), finalX);
            ObjectAnimator animY = ObjectAnimator.ofFloat(item, "y", item.getY(), finalY);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(item, "alpha", 0f, 1f);
            ObjectAnimator sX = ObjectAnimator.ofFloat(item, "scaleX", 0.1f, 1f);
            ObjectAnimator sY = ObjectAnimator.ofFloat(item, "scaleY", 0.1f, 1f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animX, animY, alpha, sX, sY);
            set.setDuration(ANIM_DURATION);
            set.setInterpolator(new OvershootInterpolator(1.1f));
            set.setStartDelay(i * 30L);
            set.start();
        }
    }


    private void collapseMenu() {
        if (!isExpanded) return;
        isExpanded = false;

        int[] fabLoc = new int[2];
        fabMain.getLocationOnScreen(fabLoc);
        int[] containerLoc = new int[2];
        fabContainer.getLocationOnScreen(containerLoc);

        float fabCenterX = fabLoc[0] - containerLoc[0] + fabMain.getWidth() / 2f;
        float fabCenterY = fabLoc[1] - containerLoc[1] + fabMain.getHeight() / 2f;

        for (int i = 0; i < spawnedViews.size(); i++) {
            View v = spawnedViews.get(i);

            ObjectAnimator animX = ObjectAnimator.ofFloat(v, "x", v.getX(), fabCenterX - v.getWidth() / 2f);
            ObjectAnimator animY = ObjectAnimator.ofFloat(v, "y", v.getY(), fabCenterY - v.getHeight() / 2f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
            ObjectAnimator sX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.2f);
            ObjectAnimator sY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.2f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animX, animY, alpha, sX, sY);
            set.setDuration(COLLAPSE_DURATION);
            set.setStartDelay(i * 20L);
            set.start();

            final int delay = COLLAPSE_DURATION + i * 20;
            v.postDelayed(() -> fabContainer.removeView(v), delay + 20L);
        }
        spawnedViews.clear();
    }

    private void attemptCall(String number) {
        if (number == null || number.trim().isEmpty()) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }
        String telUri = "tel:" + number.trim();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(telUri));
            try {
                startActivity(intent);
            } catch (SecurityException se) {
                startDial(number);
            }
        } else {
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
            startDial(number);
        }
    }

    private void startDial(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        startActivity(intent);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
