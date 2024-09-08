package vn.edu.usth.groupproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Toast;


public class CameraActivity extends AppCompatActivity {

    private ImageButton captureButton, imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection);

        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(v -> {
            Toast.makeText(this, "Capturing...", Toast.LENGTH_SHORT).show();
        });

        imagePicker = findViewById(R.id.image_picker);
        imagePicker.setOnClickListener(v -> {
            Toast.makeText(this, "Picking", Toast.LENGTH_SHORT).show();
        });
    }
}