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

        // Get Image from CameraActivity or Gallery
        image = getIntent();
        loadInspectionLayout(); // Set up the first layout

        dialog = new Dialog(InspectionActivity.this);
        dialog.setContentView(R.layout.discard_image_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(InspectionActivity.this, R.drawable.discard_dialog));

        dialogCancel = dialog.findViewById(R.id.inspection_dialog_cancel);
        dialogYes = dialog.findViewById(R.id.inspection_dialog_yes);

        dialogCancel.setOnClickListener(view -> dialog.dismiss());

        dialogYes.setOnClickListener(view -> {
            if (imgUri != null) {
                deleteImage();
            }
            goBack();
        });
    }


    private void goBack() {
        Intent intent = new Intent(InspectionActivity.this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    private void deleteImage() {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        if (imgUri != null) {
            contentResolver.delete(imgUri, null, null);
        }
    }

    private void loadInspectionLayout() {
        setContentView(R.layout.activity_inspection);

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
            setContentView(R.layout.pop_up_detection);
            setupSecondLayout();
        });
    }

    private void setupSecondLayout() {
        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(view -> {
            loadInspectionLayout();
        });
    }

}
