package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Admin.UpdateConsultantAdmin;
import com.agriberriesmx.agriberries.POJO.Consultant;
import com.agriberriesmx.agriberries.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConsultantAdapter extends RecyclerView.Adapter<ConsultantAdapter.ViewHolder> {
    private final List<Consultant> consultantList;

    public ConsultantAdapter(List<Consultant> consultantList) {
        this.consultantList = consultantList;
    }

    @NonNull
    @Override
    public ConsultantAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consultant, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultantAdapter.ViewHolder holder, int position) {
        holder.setup(consultantList.get(position));
    }

    @Override
    public int getItemCount() {
        return consultantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cvConsultant;
        private final TextView tvName, tvPhone, tvCategory, tvDeletionDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            cvConsultant = itemView.findViewById(R.id.cvConsultant);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDeletionDate = itemView.findViewById(R.id.tvDeletionDate);
        }

        public void setup(Consultant consultant) {
            // Get context
            Context context = itemView.getContext();

            // Initialize views
            tvName.setText(consultant.getName());
            tvPhone.setText(consultant.getPhone());
            tvCategory.setText(consultant.getTextCategory(context));
            if (consultant.isBlocked()) tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_circle, 0, 0, 0);
            else tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_green_circle, 0, 0, 0);
            if (consultant.getDeleted() != null) {
                // Show text view and set text
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvDeletionDate.setVisibility(View.VISIBLE);
                tvDeletionDate.setText(sdf.format(consultant.getDeleted()));
            } else tvDeletionDate.setVisibility(View.GONE);

            // Add listeners
            cvConsultant.setOnClickListener(v -> changeActivity(context, consultant));
        }

        private void changeActivity(Context context, Consultant consultant) {
            // Connect to Firebase Auth
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            // Verify who is the current user
            if (currentUser != null && (!consultant.getId().equals("dgaWY9EDkcYaLsNrtHwNSFkpErS2") ||
                    currentUser.getUid().equals("dgaWY9EDkcYaLsNrtHwNSFkpErS2"))) {
                Intent intent = new Intent(context, UpdateConsultantAdmin.class);
                intent.putExtra("consultant", consultant);
                intent.putExtra("superAdmin", currentUser.getUid().equals("dgaWY9EDkcYaLsNrtHwNSFkpErS2"));
                context.startActivity(intent);
            } else Toast.makeText(context, context.getResources().getString(R.string.cannot_modify_this_user), Toast.LENGTH_SHORT).show();
        }

    }

}
