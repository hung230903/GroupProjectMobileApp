package vn.edu.usth.groupproject;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InspectionActivity extends AppCompatActivity {
    ImageButton back, upload;
    ImageView img;
    Uri imgUri;
    Dialog dialog;
    Intent image;
    Button dialogCancel, dialogYes, closeButton;

    static final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inspection);

        // Get Image from CameraActivity or Gallery
        image = getIntent();
        // Set ImageView
        img = findViewById(R.id.img_inspection);
        String imgUriString = image.getStringExtra("imageUri");
        if (imgUriString != null) {
            imgUri = Uri.parse(imgUriString);
            if (image.getStringExtra("name") == null)
                getContentResolver().takePersistableUriPermission(imgUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            img.setImageURI(imgUri);
        }
        loadInspectionLayout(); // Set up the first layout

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
        back = findViewById(R.id.back_inspection);
        back.setOnClickListener(view -> dialog.show());

        upload = findViewById(R.id.upload_inspection);
        upload.setOnClickListener(view -> {
            Toast.makeText(this, "Uploading image to server", Toast.LENGTH_SHORT).show();
            try {
                postImage(imgUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void postImage(Uri imgUri) throws IOException {
        // Get the image file from the image uri
        File imgFile = getFileFromUri(imgUri);

        // Create request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "yolov4")
                .addFormDataPart("image", "img.jpeg",
                        RequestBody.create(imgFile, MediaType.parse("image/jpeg")))
                .build();
        System.out.println(imgFile);
        System.out.println("Request Body: " + requestBody.toString());


        // Create the request
        Request request = new Request.Builder()
                .url("http://192.168.1.8:8000/api/v1/detection") // This works on my machine, I don't know about the others :v
                .post(requestBody)
                .addHeader("Content-Type", "multipart/form-data")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("Failed to make request: " + e);
                runOnUiThread(() -> Toast.makeText(InspectionActivity.this, "Failed to make request.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    JSONObject json;

                    // Load the image into an immutable bitmap from uri
                    InputStream inputStream = getContentResolver().openInputStream(imgUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    // Convert immutable bitmap to mutable bitmap
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    try {
                        json = new JSONObject(responseData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(json);
                    // Get the bounding box from the json and draw it to a bitmap
                    drawImageBoundingBox(json, bitmap);

                    // Fix bug image rotation after drawing with canvas
                    if (bitmap.getWidth() > bitmap.getHeight()) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                                matrix, true);
                    }
                    Bitmap finalBitmap = bitmap;

                    // Display the result on the main thread
                    runOnUiThread(() ->
                            img.setImageBitmap(finalBitmap));
                    Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
                } else {
                    runOnUiThread(() -> Toast.makeText(InspectionActivity.this, "Request failed: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void drawImageBoundingBox(JSONObject json, Bitmap bitmap) {
        try {
            JSONArray predictions = json.getJSONArray("predictions");

            // Initiate canvas and paint to be able to draw
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(8);
            paint.setStyle(Paint.Style.STROKE);

            if (predictions.length() == 0) {
                runOnUiThread(() -> Toast.makeText(this, "No object detected", Toast.LENGTH_SHORT).show());
                return;
            }


            // Get the coordinates and draw
            for (int i = 0; i < predictions.length(); i++) {
                JSONObject prediction = predictions.getJSONObject(i);
                JSONObject bbox = prediction.getJSONObject("bbox");
                int x1 = bbox.getInt("x1");
                int x2 = bbox.getInt("x2");
                int y2 = bbox.getInt("y2");
                int y1 = bbox.getInt("y1");

                String label = prediction.getString("label");
                paint.setTextSize(100);

                canvas.drawRect(x1, y1, x2, y2, paint);
                System.out.println(x1 + " " + y1 + " " + x2 + " " + y2);

                // Save the canvas before draw the rotated text
                canvas.save();
                canvas.rotate(-90, x1, y2);
                canvas.drawText(label, x1-10, y2-10, paint);
                canvas.restore();
            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // Create temp file to store the image information from the uri
    private File getFileFromUri(Uri uri) throws IOException {
        File tempFile;
        InputStream inputStream = getContentResolver().openInputStream(uri);
        tempFile = File.createTempFile("image", ".jpg", getCacheDir());
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        inputStream.close();
        return tempFile;
    }


    private void setUpPopUpLayout() {
        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(view -> {
            setContentView(R.layout.activity_inspection);
            img = findViewById(R.id.img_inspection);
            loadInspectionLayout();
            img.setImageURI(imgUri);
        });
    }

}
