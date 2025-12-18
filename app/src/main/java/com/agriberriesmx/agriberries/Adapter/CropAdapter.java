package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Admin.UpdateCropAdmin;
import com.agriberriesmx.agriberries.POJO.Crop;
import com.agriberriesmx.agriberries.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.ViewHolder> {
    private final List<Crop> cropList;

    public CropAdapter(List<Crop> cropList) {
        this.cropList = cropList;
    }

    @NonNull
    @Override
    public CropAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropAdapter.ViewHolder holder, int position) {
        holder.setup(cropList.get(position));
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cvCrop;
        private final TextView tvName, tvTypes, tvDeletionDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            cvCrop = itemView.findViewById(R.id.cvCrop);
            tvName = itemView.findViewById(R.id.tvName);
            tvTypes = itemView.findViewById(R.id.tvTypes);
            tvDeletionDate = itemView.findViewById(R.id.tvDeletionDate);
        }

        public void setup(Crop crop) {
            // Get context
            Context context = itemView.getContext();

            // Initialize views
            tvName.setText(crop.getName());
            tvTypes.setText(String.join(", ", crop.getTypes()));
            if (crop.getDeleted() != null) {
                // Show text view and set text
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvDeletionDate.setVisibility(View.VISIBLE);
                tvDeletionDate.setText(sdf.format(crop.getDeleted()));
            } else tvDeletionDate.setVisibility(View.GONE);

            // Add listeners
            cvCrop.setOnClickListener(v -> changeActivity(context, crop));
        }

        private void changeActivity(Context context, Crop crop) {
            // Create intent and change
            Intent intent = new Intent(context, UpdateCropAdmin.class);
            intent.putExtra("crop", crop);
            context.startActivity(intent);
        }

    }

}
