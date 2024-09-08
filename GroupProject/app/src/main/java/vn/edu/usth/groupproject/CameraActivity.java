package vn.edu.usth.groupproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;


public class CameraActivity extends AppCompatActivity {
    ImageButton captureButton, imagePicker;
    Uri imgUri;
    ActivityResultLauncher<Intent> resultLauncher;


    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection);

        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(CameraActivity.this, InspectionActivity.class);
            startActivity(intent);
            finish();
        });

        imagePicker = findViewById(R.id.image_picker);
        imagePicker.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            resultLauncher.launch(intent);
        });
        registerResult();

    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        assert o.getData() != null;
                        imgUri = o.getData().getData();
                        Intent move = new Intent(CameraActivity.this, InspectionActivity.class);
                        move.putExtra("imageUri", imgUri.toString());
                        startActivity(move);
                        finish();
                    }
                }
        );
    }


}
