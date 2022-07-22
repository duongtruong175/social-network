package vn.hust.socialnetwork.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orhanobut.hawk.Hawk;

import java.util.Map;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.ui.groupdetail.requestjoin.RequestJoinGroupActivity;
import vn.hust.socialnetwork.ui.message.MessageActivity;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.relation.RelationActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_NOTIFICATION = "fcm_notification";
    public static final String CHANNEL_MESSAGE = "fcm_message";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // check login
        boolean isLogin = Hawk.get(AppSharedPreferences.IS_LOGIN_KEY, false);
        int userLoginId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        String nameLogin = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, "");

        if (isLogin && userLoginId != 0 && !nameLogin.isEmpty()) {
            // handle Data message notification
            Map<String, String> data = message.getData();
            // notification to device
            sendNotification(data);
        }
    }

    private void sendNotification(Map<String, String> data) {
        String title = data.get("title");
        String body = data.get("body");
        String type = data.get("type");
        String url = data.get("url");
        if (title != null && body != null && type != null && url != null) {
            if (type.equals("chat")) {
                if (!ContextExtension.isAppRunning(this)) {
                    String[] s = url.split("/");
                    try {
                        Intent intent = new Intent(this, MessageActivity.class);
                        intent.putExtra("user_id", Integer.parseInt(s[1]));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(CHANNEL_MESSAGE, "Notice", NotificationManager.IMPORTANCE_DEFAULT);
                            notificationManager.createNotificationChannel(channel);
                        }
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_MESSAGE)
                                .setContentTitle(title)
                                .setContentText(body)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setOngoing(false)
                                .setSmallIcon(R.drawable.splash_logo)
                                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                                .setWhen(System.currentTimeMillis())
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        int notificationId = 6;
                        notificationManager.notify(notificationId, builder.build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (type.equals("notification")) {
                String[] s = url.split("/");
                try {
                    Intent intent = null;
                    if (s.length == 1) {
                        if (s[0].equals("request_friend")) {
                            intent = new Intent(this, RelationActivity.class);
                            intent.putExtra("navigation_to", "add_friend");
                        }
                    } else if (s.length == 2) {
                        try {
                            int id = Integer.parseInt(s[1]);
                            if (s[0].equals("user")) {
                                intent = new Intent(this, UserDetailActivity.class);
                                intent.putExtra("user_id", id);
                            } else if (s[0].equals("post")) {
                                intent = new Intent(this, PostDetailActivity.class);
                                intent.putExtra("post_id", id);
                            } else if (s[0].equals("group")) {
                                intent = new Intent(this, RequestJoinGroupActivity.class);
                                intent.putExtra("group_id", id);
                            }
                        } catch (NumberFormatException ignored) {
                            return;
                        }
                    }
                    if (intent == null) {
                        return;
                    }
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(CHANNEL_NOTIFICATION, "Notice", NotificationManager.IMPORTANCE_DEFAULT);
                        notificationManager.createNotificationChannel(channel);
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_NOTIFICATION)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setOngoing(false)
                            .setSmallIcon(R.drawable.splash_logo)
                            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                            .setWhen(System.currentTimeMillis())
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    int notificationId = 8;
                    notificationManager.notify(notificationId, builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // save to database
        Log.v("TAG", token);
        Hawk.put(AppSharedPreferences.FCM_TOKEN, token);
    }
}
