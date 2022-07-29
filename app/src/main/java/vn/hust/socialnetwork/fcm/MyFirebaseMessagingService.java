package vn.hust.socialnetwork.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orhanobut.hawk.Hawk;

import java.util.Map;
import java.util.Random;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.ui.call.CallIncomingActivity;
import vn.hust.socialnetwork.ui.groupdetail.GroupDetailActivity;
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
    public static final String GROUP_NOTIFICATION_MESSAGE = "vn.hust.socialnetwork.message";

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

    /**
     * FCM data message
     * -> 1. Chat
     * {
     * "data": {
     *      "title": "user name",
     *      "body": "message",
     *      "type": "chat",
     *      "url": "messager/friend_id"
     * },
     *      "to": "xxxxxxxxx"
     * }
     * <p>
     * -> 2. Notification (request friend, accept friend, request group, ...)
     * {
     * "data": {
     *      "title": "Social Network",
     *      "body": "content",
     *      "type": "notification",
     *      "url": "user/{user_id} or group/{group_id} or ..."
     * },
     *      "to": "xxxxxxxxx"
     * }
     * <p>
     * -> 3. Call
     * {
     * "data": {
     *      "type": "call",
     *      "user_name": "user name",
     *      "user_avatar": "user avatar",
     *      "sender_fcm_token": "token",
     *      "meeting_type": "video or audio"
     *      "meeting_room_id": "meeting_room_id"
     * },
     *      "registration_ids": "xxxxxxxxx"
     * }
     * <p>
     * -> 4. Call Response
     * {
     * "data": {
     *      "type": "call_response",
     *      "answer": "accept or reject or cancel",
     * },
     *      "registration_ids": "xxxxxxxxx"
     * }
     */
    private void sendNotification(Map<String, String> data) {
        String type = data.get("type");
        if (type != null) {
            if (type.equals("chat")) {
                String title = data.get("title");
                String body = data.get("body");
                String url = data.get("url");
                if (!ContextExtension.isAppRunning(this)) {
                    try {
                        String[] s = url.split("/");
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
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                                .setGroup(GROUP_NOTIFICATION_MESSAGE)
                                .setAutoCancel(true)
                                .setOngoing(false)
                                .setSmallIcon(R.drawable.splash_logo)
                                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                                .setWhen(System.currentTimeMillis())
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        int notificationId = new Random().nextInt(1001) + 10; // random int in [10, 1000]
                        notificationManager.notify(notificationId, builder.build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (type.equals("notification")) {
                String title = data.get("title");
                String body = data.get("body");
                String url = data.get("url");
                try {
                    String[] s = url.split("/");
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
                                intent = new Intent(this, GroupDetailActivity.class);
                                intent.putExtra("group_id", id);
                            } else if (s[0].equals("request_join_group")) {
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
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                            .setAutoCancel(true)
                            .setOngoing(false)
                            .setSmallIcon(R.drawable.splash_logo)
                            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                            .setWhen(System.currentTimeMillis())
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    int notificationId = new Random().nextInt(2001) + 1001; // random int in [1001, 2000]
                    notificationManager.notify(notificationId, builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (type.equals("call")) {
                // get data and start CallIncomingActivity
                Intent intent = new Intent(this, CallIncomingActivity.class);
                intent.putExtra("user_name", data.get("user_name"));
                intent.putExtra("user_avatar", data.get("user_avatar"));
                intent.putExtra("sender_fcm_token", data.get("sender_fcm_token"));
                intent.putExtra("meeting_type", data.get("meeting_type"));
                intent.putExtra("meeting_room_id", data.get("meeting_room_id"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (type.equals("call_response")) {
                String answer = data.get("answer");
                // communication with CallIncomingActivity using broadcast
                Intent intent = new Intent("call_response");
                intent.putExtra("answer", answer);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
        Hawk.put(AppSharedPreferences.FCM_TOKEN, token);
    }
}
