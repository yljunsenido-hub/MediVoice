package com.example.medivoice;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import java.io.IOException;

public class PrescriptionScanner extends AppCompatActivity {

    Button btnCamera, btnGallery,saveButton,backButton;
    TextView textResult;
    Uri imageUri;
    EditText recordName;
    private static final int CAMERA_REQUEST = 101;
    private static final int GALLERY_REQUEST = 102;
    private static final int SPEECH_REQUEST_CODE = 100;
    FirebaseAuth mAuth;
    DatabaseReference userSpeechRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prescription_scanner);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        saveButton = findViewById(R.id.saveButton);
        recordName = findViewById(R.id.recordName);
        textResult = findViewById(R.id.textResult);

        // Firebase Saving Record
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PrescriptionScanner.this, HomePage.class);
            startActivity(intent);
        });

        if (currentUser != null) {
            userSpeechRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("ImageToText");
        }

        saveButton.setOnClickListener(v -> {
            String name = recordName.getText().toString().trim();
            String text = textResult.getText().toString().trim();
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
            ImageToTextRecord record = new ImageToTextRecord(name, text, date);

            userSpeechRef.child(noteId).setValue(record)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, 200);
            } else {
                openCamera();
            }
        });
        btnGallery.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            InputImage image = null;

            if (requestCode == GALLERY_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                try {
                    image = InputImage.fromFilePath(this, selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA_REQUEST) {
                try {
                    image = InputImage.fromFilePath(this, imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (image != null) {
                processTextRecognition(image);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void processTextRecognition(InputImage image) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    StringBuilder result = new StringBuilder();
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        result.append(block.getText()).append("\n");
                    }
                    textResult.setText(result.toString());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to scan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}