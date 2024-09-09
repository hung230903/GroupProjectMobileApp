package vn.edu.usth.groupproject;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class InspectionActivity extends AppCompatActivity {
    ImageButton back, upload;
    ImageView img;
    Uri imgUri;
    String imgUriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inspection);

        Intent image = getIntent();
        img = findViewById(R.id.img_inspection);
        imgUriString = image.getStringExtra("imageUri");
        if (imgUriString != null) {
            imgUri = Uri.parse(imgUriString);
            img.setImageURI(imgUri);
        }

        back = findViewById(R.id.back_inspection);
        back.setOnClickListener(view -> {
            if (image.getStringExtra("name") != null) {
                deleteImage();
            }
            Intent intent = new Intent(InspectionActivity.this, CameraActivity.class);
            startActivity(intent);
            finish();
        });

        upload = findViewById(R.id.upload_inspection);
        upload.setOnClickListener(view -> {
            Toast.makeText(InspectionActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteImage() {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.delete(imgUri, null, null);
    }
}