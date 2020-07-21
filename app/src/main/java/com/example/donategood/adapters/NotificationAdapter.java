package com.example.donategood.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donategood.R;
import com.example.donategood.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public static final String TAG = "CommentAdapter";

    private Context context;
    private List<Notification> notifications;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        notifications.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Notification> list) {
        notifications.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNotification;
        private Button btnApprove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNotification = itemView.findViewById(R.id.tvNotificationText);
            btnApprove = itemView.findViewById(R.id.btnApproveNotification);
        }

        public void bind(Notification notification) {
            tvNotification.setText("Did " + notification.getKeyUser().getUsername() + " pay you "
                    + notification.getKeyOffering().getPrice().toString() + " for your "
                    + notification.getKeyOffering().getTitle() + " ?");

            btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Notification notification = notifications.get(position);
                        Log.i(TAG, "notification clicked for offering: " + notification.getKeyOffering().getTitle());
                    }
                }
            });
        }
    }
}
