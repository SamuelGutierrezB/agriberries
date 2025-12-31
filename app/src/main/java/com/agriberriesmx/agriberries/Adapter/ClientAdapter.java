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

import com.agriberriesmx.agriberries.Admin.ShowClientAdmin;
import com.agriberriesmx.agriberries.POJO.Client;
import com.agriberriesmx.agriberries.R;
import com.agriberriesmx.agriberries.ShowClientActivity;

import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ViewHolder> {
    private final List<Client> clientList;
    private final boolean admin;

    public ClientAdapter(List<Client> clientList, boolean admin) {
        this.clientList = clientList;
        this.admin = admin;
    }

    @NonNull
    @Override
    public ClientAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientAdapter.ViewHolder holder, int position) {
        holder.setup(clientList.get(position));
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cvClient;
        private final TextView tvName, tvPhone, tvBusiness;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            cvClient = itemView.findViewById(R.id.cvClient);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvBusiness = itemView.findViewById(R.id.tvBusiness);
        }

        public void setup(Client client) {
            // Initialize views
            tvName.setText(client.getName());
            tvPhone.setText(client.getPhone());
            tvBusiness.setText(client.getBusiness());
            if (client.isBlocked())
                tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_circle, 0, 0, 0);
            else
                tvName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_green_circle, 0, 0, 0);

            // Add listeners
            cvClient.setOnClickListener(v -> changeActivity(itemView.getContext(), client));
        }

    }

    private void changeActivity(Context context, Client client) {
        // Create intent and change (sanitize id to avoid invisible characters)
        String id = client.getId();
        String sanitizedId = sanitizeId(id);

        Intent intent;
        if (admin)
            intent = new Intent(context, ShowClientAdmin.class).putExtra("id", sanitizedId);
        else
            intent = new Intent(context, ShowClientActivity.class).putExtra("client", client);
        context.startActivity(intent);
    }

    private String sanitizeId(String s) {
        if (s == null)
            return null;
        return s.replaceAll("[^A-Za-z0-9_-]", "");
    }

}
