package vn.edu.usth.groupproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class InspectionActivity extends AppCompatActivity {
    ImageButton back;
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
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InspectionActivity.this, CameraActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}