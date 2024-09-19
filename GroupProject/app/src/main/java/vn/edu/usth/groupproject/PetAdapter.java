package vn.edu.usth.groupproject;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private ArrayList<Pet> petList;

    public PetAdapter(ArrayList<Pet> petList) {
        this.petList = petList;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pet_item, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.nameTextView.setText(pet.getName());
        holder.priceTextView.setText("$" + pet.getPrice());

        // Dynamically load different images
        switch (position) {
            case 0:
                holder.petImageView.setImageResource(R.drawable.sample_pet_image_1);
                break;
            case 1:
                holder.petImageView.setImageResource(R.drawable.sample_pet_image_2);
                break;
            case 2:
                holder.petImageView.setImageResource(R.drawable.sample_pet_image_3);
                break;
            default:
                holder.petImageView.setImageResource(R.drawable.sample_pet_image_1); // Default image
                break;
        }
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView;
        ImageView petImageView;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.petName);
            priceTextView = itemView.findViewById(R.id.petPrice);
            petImageView = itemView.findViewById(R.id.petImage);
        }
    }
}
