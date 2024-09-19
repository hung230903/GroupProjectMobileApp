package vn.edu.usth.groupproject;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import vn.edu.usth.groupproject.databinding.ActivityObjectDetectionBinding;


public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int GALLERY_PERMISSION_CODE = 2;
    ImageButton captureButton, imagePicker;
    ActivityResultLauncher<Intent> resultLauncher;
    ActivityObjectDetectionBinding binding;
    ProcessCameraProvider cameraProvider;
    ImageCapture imageCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjectDetectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerCameraPermit();
        registerResult();

        // Gradient Background Animation
        ConstraintLayout constraintLayout = findViewById(R.id.gradient_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        // Image Buttons
        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(v ->
                capture());

        imagePicker = findViewById(R.id.image_picker);
        imagePicker.setOnClickListener(v -> {
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                registerGalleryPermit();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                    openGallery();
                }
            }
        });
    }

    private void registerGalleryPermit() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_CODE);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Please allow Files and Media permission in the app settings to choose image from gallery.")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private void openGallery() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    private void registerCameraPermit() {
        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            provideCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    provideCamera();
                }
                break;
            case GALLERY_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                        openGallery();
                    }
                }
                break;
        }
    }

    private void provideCamera() {
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
                        Uri imgUri = o.getData().getData();
                        if (imgUri != null) transferImage(imgUri);
                    }
                }
        );
    }

    private void transferImage(@NonNull Uri imgUri) {
        Intent move = new Intent(CameraActivity.this, InspectionActivity.class);
        move.putExtra("imageUri", imgUri.toString());
        move.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
