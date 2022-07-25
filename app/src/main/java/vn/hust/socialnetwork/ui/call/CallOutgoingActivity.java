package vn.hust.socialnetwork.ui.call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.hawk.Hawk;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.fcm.DataCall;
import vn.hust.socialnetwork.models.fcm.DataCallResponse;
import vn.hust.socialnetwork.models.fcm.DataMessageSender;
import vn.hust.socialnetwork.models.fcm.FCMResponse;
import vn.hust.socialnetwork.models.fcm.Token;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.NotificationService;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.NotificationExtension;

public class CallOutgoingActivity extends AppCompatActivity {

    private NotificationService notificationService;

    private AppCompatTextView tvUserName;
    private CircleImageView civAvatar;
    private AppCompatImageView ivMeetingType, btnCancel;

    private User user;
    private String senderFcmToken, friendFcmToken;
    private String meetingType;
    private String meetingRoomId;
    private CountDownTimer countDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_call_outgoing);

        // api
        notificationService = NotificationExtension.getFCMApi();

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getParcelable("user");
            meetingType = extras.getString("meeting_type");
        }
        senderFcmToken = Hawk.get(AppSharedPreferences.FCM_TOKEN, "");

        if (user == null || meetingType == null || senderFcmToken.isEmpty()) {
            Toast.makeText(CallOutgoingActivity.this, R.string.init_call_error, Toast.LENGTH_SHORT).show();
            finish();
        }

        // binding
        tvUserName = findViewById(R.id.tv_user_name);
        civAvatar = findViewById(R.id.civ_avatar);
        ivMeetingType = findViewById(R.id.iv_meeting_type);
        btnCancel = findViewById(R.id.btn_cancel);

        // init
        if (meetingType.equals("audio")) {
            ivMeetingType.setImageResource(R.drawable.ic_baseline_call_24);
        } else {
            ivMeetingType.setImageResource(R.drawable.ic_round_video_cam_24);
        }
        tvUserName.setText(user.getName());
        Glide.with(CallOutgoingActivity.this)
                .asBitmap()
                .load(user.getAvatar())
                .error(R.drawable.default_avatar)
                .into(civAvatar);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCalling();
            }
        });

        // get fcm token of friend and call
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("fcmTokens");
        Query query = tokens.orderByKey().equalTo(String.valueOf(user.getId()));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Token token = snap.getValue(Token.class);
                        if (token == null || token.getToken().isEmpty()) {
                            Toast.makeText(CallOutgoingActivity.this, user.getName() + " không sẵn sàng cho cuộc gọi", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            friendFcmToken = token.getToken();
                            break;
                        }
                    }
                    if (friendFcmToken != null && !friendFcmToken.isEmpty()) {
                        initMeeting();
                    } else {
                        Toast.makeText(CallOutgoingActivity.this, user.getName() + " không sẵn sàng cho cuộc gọi", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CallOutgoingActivity.this, user.getName() + " không sẵn sàng cho cuộc gọi", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CallOutgoingActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void countDownCall() {
        // after 30s, receiver don't accept -> cancel
        countDown = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                cancelCalling();
            }
        };
        countDown.start();
    }

    private void initMeeting() {
        meetingRoomId = user.getId() + "_" + UUID.randomUUID().toString();
        DataCall data = new DataCall(
                "call",
                Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, ""),
                Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, ""),
                senderFcmToken,
                meetingType,
                meetingRoomId
        );
        DataMessageSender messageSender = new DataMessageSender(data, friendFcmToken);

        sendCallMessage(messageSender, "call");
    }

    private void cancelCalling() {
        DataCallResponse data = new DataCallResponse("call_response", "cancel");
        DataMessageSender messageSender = new DataMessageSender(data, friendFcmToken);

        sendCallMessage(messageSender, "call_response");
    }

    private void sendCallMessage(DataMessageSender messageSender, String messageType) {
        Call<FCMResponse> call = notificationService.sendFCMNotification(messageSender);
        call.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.isSuccessful()) {
                    if (messageType.equals("call")) {
                        countDownCall();
                    }
                    if (messageType.equals("call_response")) {
                        Toast.makeText(CallOutgoingActivity.this, R.string.call_cancel_success, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CallOutgoingActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                call.cancel();
                Toast.makeText(CallOutgoingActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // BroadcastReceiver
    private BroadcastReceiver callResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String answer = intent.getStringExtra("answer");
            if (answer != null && answer.equals("accept")) {
                // navigation call activity
                URL serverURL = null;
                try {
                    serverURL = new URL("https://meet.jit.si");
                } catch (MalformedURLException e) {
                    Toast.makeText(CallOutgoingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
                JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                builder.setServerURL(serverURL);
                builder.setWelcomePageEnabled(false);
                builder.setRoom(meetingRoomId);
                if (meetingType.equals("audio")) {
                    builder.setVideoMuted(true);
                }
                JitsiMeetActivity.launch(CallOutgoingActivity.this, builder.build());

                finish();
            } else if (answer != null && answer.equals("reject")) {
                Toast.makeText(context, R.string.call_response_reject, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(CallOutgoingActivity.this).registerReceiver(callResponseReceiver, new IntentFilter("call_response"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(CallOutgoingActivity.this).unregisterReceiver(callResponseReceiver);
    }

    @Override
    public void finish() {
        if (countDown != null) {
            countDown.cancel();
        }
        super.finish();
        overridePendingTransition(0, 0);
    }
}