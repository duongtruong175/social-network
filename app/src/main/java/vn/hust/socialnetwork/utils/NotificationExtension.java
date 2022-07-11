package vn.hust.socialnetwork.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import vn.hust.socialnetwork.R;

public class NotificationExtension {

    public static final String CHANNEL_POST_SUCCESS = "post_success";
    public static final String CHANNEL_MESSAGE = "message";

    /**
     * Show a notification after upload post success
     */
    public static void showPostSuccess(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_POST_SUCCESS, "Notice", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_POST_SUCCESS)
                .setContentTitle("Đăng bài thành công")
                .setAutoCancel(true)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setWhen(System.currentTimeMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int notificationId = 1;
        notificationManager.notify(notificationId, builder.build());
    }
}
