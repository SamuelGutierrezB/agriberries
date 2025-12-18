package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Admin.UpdatePlagueAdmin;
import com.agriberriesmx.agriberries.POJO.Plague;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.Utils.Formatting;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlagueAdapter extends RecyclerView.Adapter<PlagueAdapter.ViewHolder> {
    private final List<Plague> plagueList;

    public PlagueAdapter(List<Plague> plagueList) {
        this.plagueList = plagueList;
    }

    @NonNull
    @Override
    public PlagueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plague, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlagueAdapter.ViewHolder holder, int position) {
        holder.setup(plagueList.get(position));
    }

    @Override
    public int getItemCount() {
        return plagueList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cvPlague;
        private final TextView tvName, tvDeletionDate;
        private final LinearLayout linearLayoutCropsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            cvPlague = itemView.findViewById(R.id.cvPlague);
            tvName = itemView.findViewById(R.id.tvName);
            tvDeletionDate = itemView.findViewById(R.id.tvDeletionDate);
            linearLayoutCropsContainer = itemView.findViewById(R.id.linearLayoutCropsContainer);
        }

        public void setup(Plague plague) {
            // Initialize views
            tvName.setText(Formatting.capitalizeFirstLetter(plague.getName()));
            if (plague.getDeleted() != null) {
                // Show text view and set text
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvDeletionDate.setVisibility(View.VISIBLE);
                tvDeletionDate.setText(sdf.format(plague.getDeleted()));
            } else tvDeletionDate.setVisibility(View.GONE);

            // Add image views to crops container
            linearLayoutCropsContainer.removeAllViews();
            for (String crop : plague.getCrops()) {
                // Create image view and get context
                Context context = itemView.getContext();
                ImageView ivCrop = new ImageView(context);

                // Define image
                if (crop.equals(context.getResources().getString(R.string.agave)))
                    ivCrop.setBackgroundResource(R.drawable.ic_agave);
                else if (crop.equals(context.getResources().getString(R.string.avocado)))
                    ivCrop.setBackgroundResource(R.drawable.ic_avocado);
                else if (crop.equals(context.getResources().getString(R.string.blueberry)))
                    ivCrop.setBackgroundResource(R.drawable.ic_blueberry);
                else if (crop.equals(context.getResources().getString(R.string.raspberry)))
                    ivCrop.setBackgroundResource(R.drawable.ic_raspberry);
                else if (crop.equals(context.getResources().getString(R.string.strawberry)))
                    ivCrop.setBackgroundResource(R.drawable.ic_strawberry);
                else if (crop.equals(context.getResources().getString(R.string.blackberry)))
                    ivCrop.setBackgroundResource(R.drawable.ic_blackberry);
                else if (crop.equals(context.getResources().getString(R.string.fig)))
                    ivCrop.setBackgroundResource(R.drawable.ic_fig);

                // Add margins
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 5, 0);
                ivCrop.setLayoutParams(params);

                // Add image view to crops container
                linearLayoutCropsContainer.addView(ivCrop);
            }

            // Add listeners
            cvPlague.setOnClickListener(v -> changeActivity(itemView.getContext(), plague));
        }

        private void changeActivity(Context context, Plague plague) {
            // Change activity and send the plague
            Intent intent = new Intent(context, UpdatePlagueAdmin.class);
            intent.putExtra("plague", plague);
            context.startActivity(intent);
        }

    }

}
