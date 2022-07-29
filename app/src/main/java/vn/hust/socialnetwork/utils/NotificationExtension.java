package vn.hust.socialnetwork.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.fcm.Data;
import vn.hust.socialnetwork.models.fcm.DataMessageSender;
import vn.hust.socialnetwork.models.fcm.FCMResponse;
import vn.hust.socialnetwork.models.fcm.Token;
import vn.hust.socialnetwork.network.NotificationService;

public class NotificationExtension {

    private static final String FCM_BASE_URL = "https://fcm.googleapis.com/";
    private static Retrofit retrofit = null;

    public static final String CHANNEL_POST_SUCCESS = "post_success";
    public static final String CHANNEL_GROUP_SUCCESS = "group_success";
    public static final String CHANNEL_STORY_SUCCESS = "story_success";

    /**
     * Show a notification after upload post success
     */
    public static void showCreatePostSuccess(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_POST_SUCCESS, "Notice", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_POST_SUCCESS)
                .setContentTitle("Đăng bài thành công")
                .setAutoCancel(true)
                .setOngoing(false)
                .setSmallIcon(R.drawable.splash_logo)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setWhen(System.currentTimeMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int notificationId = 1;
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Show a notification after create group success
     */
    public static void showCreateGroupSuccess(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_GROUP_SUCCESS, "Notice", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GROUP_SUCCESS)
                .setContentTitle("Tạo nhóm thành công")
                .setAutoCancel(true)
                .setOngoing(false)
                .setSmallIcon(R.drawable.splash_logo)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setWhen(System.currentTimeMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int notificationId = 2;
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Show a notification after create story success
     */
    public static void showCreateStorySuccess(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_STORY_SUCCESS, "Notice", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STORY_SUCCESS)
                .setContentTitle("Tạo tin thành công")
                .setAutoCancel(true)
                .setOngoing(false)
                .setSmallIcon(R.drawable.splash_logo)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setWhen(System.currentTimeMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int notificationId = 3;
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Send a notification to FCM
     */
    public static NotificationService getFCMApi() {
        retrofit = new Retrofit.Builder()
                .baseUrl(FCM_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(NotificationService.class);
    }

    public static void sendFCMNotification(int receiverId, Data data) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("fcmTokens");
        Query query = tokens.orderByKey().equalTo(String.valueOf(receiverId));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // one value
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Token token = snap.getValue(Token.class);
                    if (token != null && !token.getToken().isEmpty()) {
                        DataMessageSender messageSender = new DataMessageSender(data, token.getToken());
                        NotificationService fcmApi = getFCMApi();
                        Call<FCMResponse> call = fcmApi.sendFCMNotification(messageSender);
                        call.enqueue(new Callback<FCMResponse>() {
                            @Override
                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                // nothing
                            }

                            @Override
                            public void onFailure(Call<FCMResponse> call, Throwable t) {
                                call.cancel();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
