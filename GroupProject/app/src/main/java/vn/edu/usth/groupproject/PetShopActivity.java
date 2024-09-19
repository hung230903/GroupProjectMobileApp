// PetShopActivity.java
package vn.edu.usth.groupproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PetShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Pet> petList;
    private PetAdapter petAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_shop);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        petList = new ArrayList<>();
        // Add some sample pets
        petList.add(new Pet("Siberian Husky", 150));
        petList.add(new Pet("Golden Retriever", 250));
        petList.add(new Pet("Poodle", 300));

        petAdapter = new PetAdapter(petList);
        recyclerView.setAdapter(petAdapter);
    }
}