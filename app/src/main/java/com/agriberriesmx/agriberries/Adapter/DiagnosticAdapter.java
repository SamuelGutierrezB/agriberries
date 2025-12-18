package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.CreateDiagnosticActivity;
import com.agriberriesmx.agriberries.POJO.Diagnostic;
import com.agriberriesmx.agriberries.POJO.Unit;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.RecommendationActivity;
import com.agriberriesmx.agriberries.ShowPdfActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DiagnosticAdapter extends RecyclerView.Adapter<DiagnosticAdapter.ViewHolder> {
    private final List<Diagnostic> diagnosticList;
    private final List<Unit> unitList;
    private final String clientId;
    private final String clientName;

    public DiagnosticAdapter(List<Diagnostic> diagnosticList, List<Unit> unitList, String clientId, String clientName) {
        this.diagnosticList = diagnosticList;
        this.unitList = unitList;
        this.clientId = clientId;
        this.clientName = clientName;
    }

    @NonNull
    @Override
    public DiagnosticAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diagnostic, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiagnosticAdapter.ViewHolder holder, int position) {
        holder.setup(position);
    }

    @Override
    public int getItemCount() {
        return diagnosticList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnit, tvDate;
        ImageButton btnPDF, btnRecommendation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnPDF = itemView.findViewById(R.id.btnPDF);
            btnRecommendation = itemView.findViewById(R.id.btnRecommendation);
        }

        public void setup(int position) {
            // Get diagnostic info and context
            Context context = itemView.getContext();
            Diagnostic diagnostic = diagnosticList.get(position);

            // Initialize views
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(diagnostic.getCreation());
            String unitName = diagnostic.getUnitName();
            if (unitName == null || unitName.isEmpty()) {
                for (Unit unit : unitList)
                    if (unit.getId().equals(diagnostic.getUnit())) {
                        tvUnit.setText(unit.getName());
                        break;
                    }
            } else {
                tvUnit.setText(unitName);
            }
            tvDate.setText(formattedDate);
            if (diagnostic.isFinished()) {
                // Show PDF button
                btnPDF.setBackgroundResource(R.drawable.ic_pdf);
                btnRecommendation.setVisibility(View.VISIBLE);
            } else {
                // Show edit button
                btnPDF.setBackgroundResource(R.drawable.ic_edit);
                btnRecommendation.setVisibility(View.GONE);
            }

            // Add listeners
            btnPDF.setOnClickListener(v -> {
                Intent intent;
                if (diagnostic.isFinished()) {
                    // Go to show pdf activity
                    intent = new Intent(context, ShowPdfActivity.class);
                    intent.putExtra("path", clientId + "/" + diagnostic.getId() + ".pdf");
                } else {
                    // Go to activity to continue the diagnostic
                    intent = new Intent(context, CreateDiagnosticActivity.class);
                    intent.putExtra("clientId", clientId);
                }

                intent.putExtra("diagnostic", diagnostic);
                intent.putExtra("clientName", clientName);
                for (Unit unit : unitList)
                    if (unit.getId().equals(diagnostic.getUnit())) {
                        intent.putExtra("unit", unit);
                        break;
                    }
                context.startActivity(intent);
            });

            btnRecommendation.setOnClickListener(v -> {
                Intent intent = new Intent(context, RecommendationActivity.class);
                intent.putExtra("clientId", clientId);
                intent.putExtra("unitId", diagnostic.getUnit());
                intent.putExtra("diagnosticId", diagnostic.getId());
                intent.putExtra("recommendation", diagnostic.getRecommendation());
                context.startActivity(intent);
            });
        }

    }

}
