package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Admin.UpdateKnowledgeAdmin;
import com.agriberriesmx.agriberries.POJO.Knowledge;
import com.agriberriesmx.agriberries.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class KnowledgeAdapter extends RecyclerView.Adapter<KnowledgeAdapter.ViewHolder> {
    private final List<Knowledge> knowledgeList;
    private final boolean admin;

    public KnowledgeAdapter(List<Knowledge> knowledgeList, boolean admin) {
        this.knowledgeList = knowledgeList;
        this.admin = admin;
    }

    @NonNull
    @Override
    public KnowledgeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_knowledge, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KnowledgeAdapter.ViewHolder holder, int position) {
        holder.setup(knowledgeList.get(position), admin);
    }

    @Override
    public int getItemCount() {
        return knowledgeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cvKnowledge;
        private final TextView tvTitle, tvLink, tvDeletionDate;
        private final LinearLayout linearLayoutCropsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            cvKnowledge = itemView.findViewById(R.id.cvKnowledge);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLink = itemView.findViewById(R.id.tvLink);
            tvDeletionDate = itemView.findViewById(R.id.tvDeletionDate);
            linearLayoutCropsContainer = itemView.findViewById(R.id.linearLayoutCropsContainer);
        }

        public void setup(Knowledge knowledge, boolean admin) {
            // Get context
            Context context = itemView.getContext();

            // Initialize views
            tvTitle.setText(knowledge.getTitle());
            tvLink.setText(knowledge.getLink());

            // Update visibility
            if (knowledge.getLink().isEmpty()) tvLink.setVisibility(View.GONE);
            else tvLink.setVisibility(View.VISIBLE);
            if (knowledge.getDeleted() != null) {
                // Show text view and set text
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvDeletionDate.setVisibility(View.VISIBLE);
                tvDeletionDate.setText(sdf.format(knowledge.getDeleted()));
            } else tvDeletionDate.setVisibility(View.GONE);

            // Add image views to crops container
            linearLayoutCropsContainer.removeAllViews();
            for (String crop : knowledge.getCrops()) {
                // Create image view
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
            if (admin) cvKnowledge.setOnClickListener(v -> changeActivity(context, knowledge));
            else cvKnowledge.setOnClickListener(v -> goToLink(context, knowledge.getLink()));
        }

        private void changeActivity(Context context, Knowledge knowledge) {
            // Create intent and change
            Intent intent = new Intent(context, UpdateKnowledgeAdmin.class);
            intent.putExtra("knowledge", knowledge);
            context.startActivity(intent);
        }


        private void goToLink(Context context, String link) {
            // Get link and start intent
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getString(R.string.invalidLink), Toast.LENGTH_SHORT).show();
            }
        }

    }

}
