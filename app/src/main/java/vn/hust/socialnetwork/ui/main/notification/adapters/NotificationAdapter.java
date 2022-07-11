package vn.hust.socialnetwork.ui.main.notification.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.notification.Notification;
import vn.hust.socialnetwork.utils.TimeExtension;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationListener onNotificationListener;

    public NotificationAdapter(Context context, List<Notification> notifications, OnNotificationListener onNotificationListener) {
        this.context = context;
        this.notifications = notifications;
        this.onNotificationListener = onNotificationListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.tvNotificationContent.setText(notification.getContent());
        holder.tvTimeNotification.setText(TimeExtension.formatTimeNotification(notification.getCreatedAt()));
        int type = notification.getType();
        if (type == 1) { // new request add friend
            holder.ivNotificationType.setImageResource(R.drawable.notification_type_add_friend);
            Glide.with(context)
                    .asBitmap()
                    .load(notification.getImageUrl())
                    .error(R.drawable.default_avatar)
                    .into(holder.civImageNotification);
        } else if (type == 2) { // new accept friend
            holder.ivNotificationType.setImageResource(R.drawable.notification_type_friend);
            Glide.with(context)
                    .asBitmap()
                    .load(notification.getImageUrl())
                    .error(R.drawable.default_avatar)
                    .into(holder.civImageNotification);
        } else if (type == 3) { // new action post
            holder.ivNotificationType.setImageResource(R.drawable.notification_type_post);
            Glide.with(context)
                    .asBitmap()
                    .load(notification.getImageUrl())
                    .error(R.drawable.default_avatar)
                    .into(holder.civImageNotification);
        } else if (type == 4) { // new action group
            holder.ivNotificationType.setImageResource(R.drawable.notification_type_group);
            Glide.with(context)
                    .asBitmap()
                    .load(notification.getImageUrl())
                    .error(R.drawable.default_group_cover)
                    .into(holder.civImageNotification);
        }
        if (notification.getNotificationStatusCode() == 0) { // not read
            holder.lRoot.setBackgroundResource(R.color.color_notification_not_read);
        } else {
            holder.lRoot.setBackgroundResource(R.color.white);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout lRoot;
        CircleImageView civImageNotification;
        AppCompatImageView ivNotificationType, ivMenu;
        AppCompatTextView tvNotificationContent, tvTimeNotification;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            lRoot = itemView.findViewById(R.id.l_root);
            civImageNotification = itemView.findViewById(R.id.civ_image_notification);
            ivNotificationType = itemView.findViewById(R.id.iv_notification_type);
            tvNotificationContent = itemView.findViewById(R.id.tv_notification_content);
            tvTimeNotification = itemView.findViewById(R.id.tv_time_notification);
            ivMenu = itemView.findViewById(R.id.iv_menu);

            if (onNotificationListener != null) {
                ivMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNotificationListener.onMenuItemClick(getBindingAdapterPosition());
                    }
                });
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNotificationListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
