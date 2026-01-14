package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.Admin.UpdateConsultantAdmin;
import com.agriberriesmx.agriberries.POJO.Consultant;
import com.agriberriesmx.agriberries.R;

import java.util.List;

public class AssignedConsultantAdapter extends RecyclerView.Adapter<AssignedConsultantAdapter.ViewHolder> {
    private final List<Consultant> consultantList;
    private final List<String> assignedConsultantList;

    public AssignedConsultantAdapter(List<Consultant> consultantList, List<String> assignedConsultantList) {
        this.consultantList = consultantList;
        this.assignedConsultantList = assignedConsultantList;
    }

    @NonNull
    @Override
    public AssignedConsultantAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assigned_consultant, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignedConsultantAdapter.ViewHolder holder, int position) {
        holder.setup(position);
    }

    @Override
    public int getItemCount() {
        return consultantList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout linearLayoutAssignedConsultant;
        private final TextView tvName;
        private final CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            linearLayoutAssignedConsultant = itemView.findViewById(R.id.linearLayoutAssignedConsultant);
            tvName = itemView.findViewById(R.id.tvName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }

        public void setup(int position) {
            // Get consultant
            Consultant consultant = consultantList.get(position);
            String id = consultant.getId();

            // Initialize views
            tvName.setText(consultant.getName());
            checkBox.setChecked(assignedConsultantList.contains(id));

            // Add listeners
            linearLayoutAssignedConsultant.setOnClickListener(v -> changeActivity(itemView.getContext(), consultant));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked)
                    if (!assignedConsultantList.contains(id))
                        assignedConsultantList.add(id);
                    else
                        assignedConsultantList.remove(id);
            });
        }

        private void changeActivity(Context context, Consultant consultant) {
            // Change activity to show more information
            Intent intent = new Intent(context, UpdateConsultantAdmin.class);
            intent.putExtra("consultant", consultant);
            context.startActivity(intent);
        }

    }

}
