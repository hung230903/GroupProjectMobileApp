package vn.edu.usth.groupproject;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

public class InspectionActivity extends AppCompatActivity {
    ImageButton back, upload;
    ImageView img;
    Uri imgUri;
    Dialog dialog;
    Intent image;
    Button dialogCancel, dialogYes, closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inspection);

        // Get Image from CameraActivity
        image = getIntent();

        // Dialog View
        dialog = new Dialog(InspectionActivity.this);
        dialog.setContentView(R.layout.discard_image_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(InspectionActivity.this, R.drawable.discard_dialog));

        dialogCancel = dialog.findViewById(R.id.inspection_dialog_cancel);
        dialogYes = dialog.findViewById(R.id.inspection_dialog_yes);

        dialogCancel.setOnClickListener(view -> dialog.dismiss());

        dialogYes.setOnClickListener(view -> {
            if (image.getStringExtra("name") != null) {
                deleteImage();
            }
            goBack();
        });

        img = findViewById(R.id.img_inspection);
        String imgUriString = image.getStringExtra("imageUri");
        if (imgUriString != null) {
            imgUri = Uri.parse(imgUriString);
            img.setImageURI(imgUri);
        }

        back = findViewById(R.id.back_inspection);
        back.setOnClickListener(view -> dialog.show());

        upload = findViewById(R.id.upload_inspection);
        upload.setOnClickListener(view -> {
            // Switch to the second layout
            setContentView(R.layout.pop_up_detection);

            // Find the close button in the second layout and set up its click listener
            closeButton = findViewById(R.id.closeButton);
            closeButton.setOnClickListener(view1 -> {
                // Switch back to the first (inspection) layout when close button is clicked
                setContentView(R.layout.activity_inspection);
                setupFirstLayout();  // Restore the logic and buttons in the first layout
            });
        });
    }

    private void setupFirstLayout() {
        // Re-setup everything related to the first layout
        img = findViewById(R.id.img_inspection);
        String imgUriString = image.getStringExtra("imageUri");
        if (imgUriString != null) {
            imgUri = Uri.parse(imgUriString);
            img.setImageURI(imgUri);
        }

        back = findViewById(R.id.back_inspection);
        back.setOnClickListener(view -> dialog.show());

        upload = findViewById(R.id.upload_inspection);
        upload.setOnClickListener(view -> {
            // Switch to the second layout again
            setContentView(R.layout.pop_up_detection);

            // Set up the close button listener for the second layout again
            closeButton = findViewById(R.id.closeButton);
            closeButton.setOnClickListener(view1 -> {
                // Switch back to the first layout
                setContentView(R.layout.activity_inspection);
                setupFirstLayout(); // Reinitialize first layout
            });
        });
    }

    private void goBack() {
        Intent intent = new Intent(InspectionActivity.this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    private void deleteImage() {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.delete(imgUri, null, null);
    }
}
