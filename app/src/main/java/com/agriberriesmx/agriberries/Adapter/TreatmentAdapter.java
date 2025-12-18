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

import com.agriberriesmx.agriberries.Admin.UpdateProductAdmin;
import com.agriberriesmx.agriberries.POJO.Treatment;
import com.agriberriesmx.agriberries.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TreatmentAdapter extends RecyclerView.Adapter<TreatmentAdapter.ViewHolder> {
    private final List<Treatment> treatmentList;

    public TreatmentAdapter(List<Treatment> treatmentList) {
        this.treatmentList = treatmentList;
    }

    @NonNull
    @Override
    public TreatmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_treatment, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreatmentAdapter.ViewHolder holder, int position) {
        holder.setup(treatmentList.get(position));
    }

    @Override
    public int getItemCount() {
        return treatmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cvTreatment;
        private final TextView tvName, tvIngredient, tvPlagues, tvDeletionDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            cvTreatment = itemView.findViewById(R.id.cvTreatment);
            tvName = itemView.findViewById(R.id.tvName);
            tvIngredient = itemView.findViewById(R.id.tvIngredient);
            tvPlagues = itemView.findViewById(R.id.tvPlagues);
            tvDeletionDate = itemView.findViewById(R.id.tvDeletionDate);
        }

        public void setup(Treatment treatment) {
            // Get formatted list
            List<String> formattedPlagues = treatment.getPlagues().stream()
                    .map(plague -> plague.substring(0, 1).toUpperCase() + plague.substring(1).toLowerCase())
                    .collect(Collectors.toList());

            // Initialize views
            tvName.setText(treatment.getName());
            tvIngredient.setText(treatment.getIngredient());
            tvPlagues.setText(String.join(", ", formattedPlagues));
            if (treatment.getDeleted() != null) {
                // Show text view and set text
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvDeletionDate.setVisibility(View.VISIBLE);
                tvDeletionDate.setText(sdf.format(treatment.getDeleted()));
            } else tvDeletionDate.setVisibility(View.GONE);

            // Add listeners
            cvTreatment.setOnClickListener(v -> changeActivity(itemView.getContext(), treatment));
        }

        private void changeActivity(Context context, Treatment treatment) {
            // Get treatment and change activity
            Intent intent = new Intent(context, UpdateProductAdmin.class);
            intent.putExtra("treatment", treatment);
            context.startActivity(intent);
        }

    }

}
