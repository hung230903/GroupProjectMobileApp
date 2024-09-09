package vn.edu.usth.groupproject;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import vn.edu.usth.groupproject.databinding.ActivityObjectDetectionBinding;


public class CameraActivity extends AppCompatActivity {
    ImageButton captureButton, imagePicker;
    Uri imgUri;
    ActivityResultLauncher<Intent> resultLauncher;
    ActivityObjectDetectionBinding binding;
    ProcessCameraProvider cameraProvider;
    ImageCapture imageCapture;


    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjectDetectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gradient Background Animation
        ConstraintLayout constraintLayout = findViewById(R.id.gradient_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        // Image Buttons
        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(v -> {
            capture();
        });

        imagePicker = findViewById(R.id.image_picker);
        imagePicker.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            resultLauncher.launch(intent);
        });
        registerResult();

        // Camera Provider
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderListenableFuture.get();
                    startCameraX(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capture() {
        if (imageCapture == null) {
            return;
        }
        String name = System.currentTimeMillis() + "_ovcam";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures");
        }

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues).build();


        Toast.makeText(this, "Taking photo, please hold the camera still", Toast.LENGTH_SHORT).show();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                if (outputFileResults.getSavedUri() != null) transferCameraImage(outputFileResults.getSavedUri(), name);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CameraActivity.this, "Error while taking photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.cameraFrame.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == CameraActivity.RESULT_CANCELED || o.getData() == null)
                            return;
                        imgUri = o.getData().getData();
                        if (imgUri != null) transferImage(imgUri);
                    }
                }
        );
    }

    private void transferImage(@NonNull Uri imgUri) {
        Intent move = new Intent(CameraActivity.this, InspectionActivity.class);
        move.putExtra("imageUri", imgUri.toString());
        startActivity(move);
        finish();
    }

    private void transferCameraImage(@NonNull Uri imgUri, String name) {
        Intent move = new Intent(CameraActivity.this, InspectionActivity.class);
        move.putExtra("imageUri", imgUri.toString());
        move.putExtra("name", name);
        startActivity(move);
        finish();
    }


}
