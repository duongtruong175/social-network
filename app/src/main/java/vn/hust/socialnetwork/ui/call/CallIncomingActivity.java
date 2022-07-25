package vn.hust.socialnetwork.ui.call;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions3.RxPermissions;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.fcm.DataCallResponse;
import vn.hust.socialnetwork.models.fcm.DataMessageSender;
import vn.hust.socialnetwork.models.fcm.FCMResponse;
import vn.hust.socialnetwork.network.NotificationService;
import vn.hust.socialnetwork.utils.NotificationExtension;

public class CallIncomingActivity extends AppCompatActivity {

    private NotificationService notificationService;

    private CircleImageView civAvatar;
    private AppCompatTextView tvUserName, tvCallType;
    private AppCompatImageView btnCancel, btnAccept;

    private String userName, userAvatar;
    private String senderFcmToken;
    private String meetingType;
    private String meetingRoomId;

    private Ringtone ringtone;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            // wake up screen
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
        setContentView(R.layout.activity_call_incoming);

        // api
        notificationService = NotificationExtension.getFCMApi();

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userName = extras.getString("user_name");
            userAvatar = extras.getString("user_avatar");
            senderFcmToken = extras.getString("sender_fcm_token");
            meetingType = extras.getString("meeting_type");
            meetingRoomId = extras.getString("meeting_room_id");
        }

        // binding
        civAvatar = findViewById(R.id.civ_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvCallType = findViewById(R.id.tv_call_type);
        btnCancel = findViewById(R.id.btn_cancel);
        btnAccept = findViewById(R.id.btn_accept);

        // init
        if (meetingType.equals("audio")) {
            tvCallType.setText(R.string.call_audio_description);
            btnAccept.setImageResource(R.drawable.ic_baseline_call_24);
        } else {
            tvCallType.setText(R.string.call_video_description);
            btnAccept.setImageResource(R.drawable.ic_round_video_cam_24);
        }
        tvUserName.setText(userName);
        Glide.with(CallIncomingActivity.this)
                .asBitmap()
                .load(userAvatar)
                .error(R.drawable.default_avatar)
                .into(civAvatar);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCallResponse("reject");
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxPermissions rxPermissions = new RxPermissions(CallIncomingActivity.this);
                if (meetingType.equals("audio")) {
                    rxPermissions.request(Manifest.permission.RECORD_AUDIO)
                            .subscribe(granted -> {
                                if (granted) {
                                    sendCallResponse("accept");
                                } else {
                                    Toast.makeText(CallIncomingActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    rxPermissions.request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                            .subscribe(granted -> {
                                if (granted) {
                                    sendCallResponse("accept");
                                } else {
                                    Toast.makeText(CallIncomingActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // play sound
        Uri callIncoming = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if (callIncoming != null) {
            ringtone = RingtoneManager.getRingtone(CallIncomingActivity.this, callIncoming);
            ringtone.play();
        }
        // make vibrator indefinitely
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            // Start without a delay
            // Vibrate for 1600 milliseconds
            // Sleep for 1000 milliseconds
            long[] pattern = {0, 1600, 1000};
            // The '0' here means to repeat indefinitely
            // '0' is actually the index at which the pattern keeps repeating from (the start)
            // To repeat the pattern from any other point, you could increase the index, e.g. '1'
            vibrator.vibrate(pattern, 0);
        }
    }

    private void sendCallResponse(String callResponse) {
        DataCallResponse data = new DataCallResponse("call_response", callResponse);
        DataMessageSender messageSender = new DataMessageSender(data, senderFcmToken);

        sendCallMessage(messageSender, callResponse);
    }

    private void sendCallMessage(DataMessageSender messageSender, String callResponse) {
        Call<FCMResponse> call = notificationService.sendFCMNotification(messageSender);
        call.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.isSuccessful()) {
                    // if accept
                    if (callResponse.equals("accept")) {
                        // navigation call activity
                        URL serverURL = null;
                        try {
                            serverURL = new URL("https://meet.jit.si");
                        } catch (MalformedURLException e) {
                            Toast.makeText(CallIncomingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoomId);
                        if (meetingType.equals("audio")) {
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(CallIncomingActivity.this, builder.build());

                        finish();
                    } else if (callResponse.equals("reject")) {
                        Toast.makeText(CallIncomingActivity.this, R.string.call_cancel_success, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CallIncomingActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                call.cancel();
                Toast.makeText(CallIncomingActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // BroadcastReceiver
    private BroadcastReceiver callResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String answer = intent.getStringExtra("answer");
            if (answer != null && answer.equals("cancel")) {
                Toast.makeText(context, R.string.call_cancel_success, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(CallIncomingActivity.this).registerReceiver(callResponseReceiver, new IntentFilter("call_response"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(CallIncomingActivity.this).unregisterReceiver(callResponseReceiver);
    }

    @Override
    public void finish() {
        if (ringtone != null) {
            ringtone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        super.finish();
        overridePendingTransition(0, 0);
    }
}