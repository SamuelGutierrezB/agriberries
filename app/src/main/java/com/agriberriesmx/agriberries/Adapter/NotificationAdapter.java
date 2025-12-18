package com.agriberriesmx.agriberries.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.agriberriesmx.agriberries.POJO.Notification;
import com.agriberriesmx.agriberries.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<Notification> notificationList;
    private final String uid;

    public NotificationAdapter(List<Notification> notificationList, String uid) {
        this.notificationList = notificationList;
        this.uid = uid;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        holder.setup(position);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayoutNotification;
        TextView tvTitle, tvText, tvMoreInfo, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link XML to Java
            linearLayoutNotification = itemView.findViewById(R.id.linearLayoutNotification);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvText = itemView.findViewById(R.id.tvText);
            tvMoreInfo = itemView.findViewById(R.id.tvMoreInfo);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void setup(int position) {
            // Get notification link and context
            Context context = itemView.getContext();
            Notification notification = notificationList.get(position);
            String link = notification.getLink();

            // Get current date
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // Get notification's begin date
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(notification.getBegin());

            // Get formatted date
            String formattedDate;
            long diffInMillis = today.getTimeInMillis() - calendarDate.getTimeInMillis();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (diffInDays == 0) {
                // Today
                formattedDate = context.getResources().getString(R.string.today);
            } else if (diffInDays == 1) {
                // Yesterday
                formattedDate = context.getResources().getString(R.string.yesterday);
            } else {
                // Get day difference
                formattedDate = context.getResources().getString(R.string.ago) + " ";
                formattedDate += diffInDays + " ";
                formattedDate += context.getResources().getString(R.string.days);
            }

            // Initialize views
            tvTitle.setText(notification.getTitle());
            tvText.setText(notification.getText());
            tvDate.setText(formattedDate);
            if (link.isEmpty()) tvMoreInfo.setVisibility(View.GONE);
            else tvMoreInfo.setVisibility(View.VISIBLE);

            if (!notification.getConsultantsSeen().contains(uid)) {
                // This notifications is unread
                Drawable leftDrawable = ContextCompat.getDrawable(context, R.drawable.ic_dot);
                tvTitle.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);
            } else tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

            // Add listeners
            linearLayoutNotification.setOnClickListener(v -> {
                // Verify if the UID it is on the list or not
                if (!notification.getConsultantsSeen().contains(uid)) {
                    // Update array to add UID
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference documentReference = db.collection("notifications").document(notification.getId());
                    documentReference.update("consultantsSeen", FieldValue.arrayUnion(uid));
                    tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }

                // Verify if there is a link
                if (!link.isEmpty()) changeActivity(context, link);
            });
        }

        private void changeActivity(Context context, String link) {
            //Get link and start intent
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getString(R.string.invalidLink), Toast.LENGTH_SHORT).show();
            }
        }

    }

}
