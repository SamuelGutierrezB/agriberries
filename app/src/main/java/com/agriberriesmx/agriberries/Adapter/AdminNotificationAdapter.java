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

import com.agriberriesmx.agriberries.Admin.UpdateNotificationAdmin;
import com.agriberriesmx.agriberries.POJO.Notification;
import com.agriberriesmx.agriberries.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {
    private final List<Notification> notificationList;

    public AdminNotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public AdminNotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_admin, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminNotificationAdapter.ViewHolder holder, int position) {
        holder.setup(position);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cvNotification;
        private final TextView tvTitle, tvText, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            cvNotification = itemView.findViewById(R.id.cvNotification);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvText = itemView.findViewById(R.id.tvText);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void setup(int position) {
            // Get notification
            Notification notification = notificationList.get(position);

            // Initialize views
            tvTitle.setText(notification.getTitle());
            tvText.setText(notification.getText());
            tvDate.setText(formatDateRange(notification.getBegin(), notification.getEnd()));

            // Add listeners
            cvNotification.setOnClickListener(v -> changeActivity(itemView.getContext(), notification));
        }

        private void changeActivity(Context context, Notification notification) {
            // Get id from notification and change activity to update it
            Intent intent = new Intent(context, UpdateNotificationAdmin.class);
            intent.putExtra("notification", notification);
            context.startActivity(intent);
        }

        private String formatDateRange(Date begin, Date end) {
            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            String beginStr = sdf.format(begin);
            String endStr = sdf.format(end);

            if (beginStr.equals(endStr)) return beginStr;
            else  return beginStr + " - " + endStr;
        }

    }

}
