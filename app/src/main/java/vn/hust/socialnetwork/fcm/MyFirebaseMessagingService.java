package vn.hust.socialnetwork.fcm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.v("TAG", "From: " + message.getFrom());
        // Check if message contains a data payload.
        if (message.getData().size() > 0) {
            Log.v("TAG", "Message data payload: " + message.getData());
        }
        // Check if message contains a notification payload.
        if (message.getNotification() != null) {
            Log.v("TAG", "Message Notification Body: " + message.getNotification().getBody());
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}
