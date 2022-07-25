package vn.hust.socialnetwork.ui.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.swipetoreply.OnSwipeToReplyListener;
import vn.hust.socialnetwork.common.swipetoreply.SwipeToReplyController;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.chat.Conversation;
import vn.hust.socialnetwork.models.chat.Message;
import vn.hust.socialnetwork.models.chat.ReplyToMessage;
import vn.hust.socialnetwork.models.fcm.Data;
import vn.hust.socialnetwork.models.fcm.DataMessageSender;
import vn.hust.socialnetwork.models.fcm.FCMResponse;
import vn.hust.socialnetwork.models.fcm.Token;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.ChatService;
import vn.hust.socialnetwork.network.NotificationService;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.ui.call.CallOutgoingActivity;
import vn.hust.socialnetwork.ui.message.adapters.MessageAdapter;
import vn.hust.socialnetwork.ui.message.adapters.OnMessageListener;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.NotificationExtension;
import vn.hust.socialnetwork.utils.RequestCodeResultActivity;

public class MessageActivity extends AppCompatActivity {

    private UserProfileService userProfileService;
    private ChatService chatService;

    private DatabaseReference messagesReference;

    private User friend;
    private int friendId;
    private int myId;
    private String myName;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private ReplyToMessage replyObject;
    private String roomChatId;
    private boolean isAddOrFirstLoadMessage = true; // use to handle logic show/hide button "scroll to end"

    private EmojiPopup emojiPopup;
    private LinearProgressIndicator pbLoading;
    private AppCompatImageView ivBackToolbar, ivCallPhone, ivCallVideo, ivCancelReply, ivAddEmojiMessage, ivSendMessage;
    private CircleImageView civFriendAvatar;
    private AppCompatTextView tvFriendName, tvReplyUserName, tvReplyMessageContent;
    private RecyclerView rvMessage;
    private ConstraintLayout lRoot, lInputReplyMessage;
    private AppCompatEditText etContentMessage;
    private AppCompatImageButton btnScrollEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            friendId = extras.getInt("user_id");
        } else {
            friendId = 0;
        }

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);
        chatService = ApiClient.getClient().create(ChatService.class);

        // binding
        lRoot = findViewById(R.id.l_root);
        pbLoading = findViewById(R.id.pb_loading);
        ivBackToolbar = findViewById(R.id.iv_back_toolbar);
        ivCallPhone = findViewById(R.id.iv_call_phone);
        ivCallVideo = findViewById(R.id.iv_call_video);
        ivCancelReply = findViewById(R.id.iv_cancel_reply);
        ivAddEmojiMessage = findViewById(R.id.iv_add_emoji_message);
        ivSendMessage = findViewById(R.id.iv_send_message);
        civFriendAvatar = findViewById(R.id.civ_friend_avatar);
        tvFriendName = findViewById(R.id.tv_friend_name);
        tvReplyUserName = findViewById(R.id.tv_reply_user_name);
        tvReplyMessageContent = findViewById(R.id.tv_reply_message_content);
        rvMessage = findViewById(R.id.rv_message);
        lInputReplyMessage = findViewById(R.id.l_input_reply_message);
        etContentMessage = findViewById(R.id.et_content_message);
        btnScrollEnd = findViewById(R.id.btn_scroll_end);

        // init view
        emojiPopup = EmojiPopup.Builder
                .fromRootView(lRoot)
                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        ivAddEmojiMessage.setColorFilter(ContextCompat.getColor(MessageActivity.this, R.color.color_drawable_post));
                    }
                })
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        ivAddEmojiMessage.setColorFilter(ContextCompat.getColor(MessageActivity.this, R.color.color_text_highlight));
                    }
                })
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .build(etContentMessage);

        myId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        myName = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, "");
        replyObject = new ReplyToMessage(0, "", "");
        ivSendMessage.setEnabled(false);

        ivBackToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // handle edittext comment
        etContentMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                }
                ContextExtension.showKeyboard(etContentMessage);
            }
        });

        etContentMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etContentMessage.getText().toString().trim().length() > 0) {
                    ivSendMessage.setEnabled(true);
                } else {
                    ivSendMessage.setEnabled(false);
                }
            }
        });

        ivCancelReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearReplyMessage();
            }
        });

        ivCallPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call a phone
                RxPermissions rxPermissions = new RxPermissions(MessageActivity.this);
                rxPermissions.request(Manifest.permission.RECORD_AUDIO)
                        .subscribe(granted -> {
                            if (granted) {
                                initiateAudioMeeting();
                            } else {
                                Toast.makeText(MessageActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        ivCallVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call a video
                RxPermissions rxPermissions = new RxPermissions(MessageActivity.this);
                rxPermissions.request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) {
                                initiateVideoMeeting();
                            } else {
                                Toast.makeText(MessageActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        ivAddEmojiMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiPopup.toggle();
            }
        });

        // create a comment
        ivSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        btnScrollEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rvMessage != null) {
                    rvMessage.smoothScrollToPosition(messages.size() - 1);
                    btnScrollEnd.setVisibility(View.INVISIBLE);
                }
            }
        });

        // get data
        getUserDetail();
    }

    private void initiateAudioMeeting() {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MessageActivity.this);
        Intent intent = new Intent(MessageActivity.this, CallOutgoingActivity.class);
        intent.putExtra("user", friend);
        intent.putExtra("meeting_type", "audio");
        startActivity(intent, options.toBundle());
    }

    private void initiateVideoMeeting() {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MessageActivity.this);
        Intent intent = new Intent(MessageActivity.this, CallOutgoingActivity.class);
        intent.putExtra("user", friend);
        intent.putExtra("meeting_type", "video");
        startActivity(intent, options.toBundle());
    }

    private void handleView() {
        Glide.with(MessageActivity.this)
                .asBitmap()
                .load(friend.getAvatar())
                .error(R.drawable.default_avatar)
                .into(civFriendAvatar);
        tvFriendName.setText(friend.getName());
        // check call permission
        ivCallPhone.setVisibility(View.VISIBLE);
        ivCallVideo.setVisibility(View.VISIBLE);
    }

    private void setupRecycleView() {
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(MessageActivity.this, friend, messages, new OnMessageListener() {
            @Override
            public void onUserClick(int position) {
                // open user detail
                Intent intent = new Intent(MessageActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", friend.getId());
                startActivity(intent);
            }

            @Override
            public void onLongClickContent(int react, int position) {
                // update react message
                updateReactMessage(react, position);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(MessageActivity.this);
        layoutManager.setStackFromEnd(true);
        rvMessage.setLayoutManager(layoutManager);
        rvMessage.setHasFixedSize(true);
        rvMessage.setAdapter(messageAdapter);

        // Swipe to reply
        SwipeToReplyController swipeToReplyController = new SwipeToReplyController(MessageActivity.this, new OnSwipeToReplyListener() {
            @Override
            public void onShowReplyUI(int position) {
                // Here you can handle the swipe-to-reply event
                // open reply object
                Message message = messages.get(position);
                if (message.getSenderId() == myId) { // message in right
                    replyObject.setSenderId(message.getSenderId());
                    replyObject.setSenderName(myName);
                    replyObject.setContent(message.getContent());
                } else { // message in left
                    replyObject.setSenderId(message.getSenderId());
                    replyObject.setSenderName(friend.getName());
                    replyObject.setContent(message.getContent());
                }
                if (replyObject.getSenderId() == myId) {
                    tvReplyUserName.setText("chính mình");
                } else {
                    tvReplyUserName.setText(replyObject.getSenderName());
                }
                tvReplyMessageContent.setText(replyObject.getContent());
                lInputReplyMessage.setVisibility(View.VISIBLE);
                ContextExtension.showKeyboard(etContentMessage);
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToReplyController);
        itemTouchHelper.attachToRecyclerView(rvMessage);

        rvMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    btnScrollEnd.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void getUserDetail() {
        pbLoading.setVisibility(View.VISIBLE);
        Call<BaseResponse<User>> call = userProfileService.getUserProfileById(friendId);
        call.enqueue(new Callback<BaseResponse<User>>() {
            @Override
            public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<User> res = response.body();
                    friend = res.getData();
                    // handle view
                    handleView();
                    setupRecycleView();
                    // get room chat information
                    getConversationDetail();
                } else {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(MessageActivity.this, R.string.error_get_data_for_chat, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<User>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MessageActivity.this, R.string.error_get_data_for_chat, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getConversationDetail() {
        Call<BaseResponse<Conversation>> call = chatService.createConversation(friendId);
        call.enqueue(new Callback<BaseResponse<Conversation>>() {
            @Override
            public void onResponse(Call<BaseResponse<Conversation>> call, Response<BaseResponse<Conversation>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Conversation> res = response.body();
                    roomChatId = res.getData().getRoomChatId();
                    // get a reference "messages" in realtime database (firebase)
                    messagesReference = FirebaseDatabase.getInstance().getReference("messages").child(roomChatId);
                    // get list message from firebase
                    getMessages();
                } else {
                    Toast.makeText(MessageActivity.this, R.string.error_get_data_for_chat, Toast.LENGTH_SHORT).show();
                }
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<Conversation>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(MessageActivity.this, R.string.error_get_data_for_chat, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMessages() {
        if (roomChatId != null && !roomChatId.isEmpty()) {
            messagesReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    messages.clear();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Message message = snap.getValue(Message.class);
                        messages.add(message);
                    }
                    messageAdapter.notifyDataSetChanged();

                    // new message -> show button "scroll to end"
                    if (btnScrollEnd.getVisibility() == View.INVISIBLE && messages.size() > 0) {
                        LinearLayoutManager layoutManager = (LinearLayoutManager) rvMessage.getLayoutManager();
                        if (layoutManager != null) {
                            if (isAddOrFirstLoadMessage) {
                                rvMessage.smoothScrollToPosition(messages.size() - 1);
                                isAddOrFirstLoadMessage = false;
                            } else {
                                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                                // if screen is displaying least two last message -> scroll
                                if (lastVisiblePosition + 3 >= messages.size()) {
                                    rvMessage.smoothScrollToPosition(messages.size() - 1);
                                }
                                else if (lastVisiblePosition != messages.size() - 1) {
                                    btnScrollEnd.setVisibility(View.VISIBLE);
                                    btnScrollEnd.startAnimation(AnimationUtils.loadAnimation(MessageActivity.this, R.anim.slide_in_up));
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MessageActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendMessage() {
        String messageContent = etContentMessage.getText().toString().trim();
        if (!messageContent.isEmpty() && messagesReference != null) {
            String messageId = messagesReference.push().getKey();
            if (messageId != null) {
                isAddOrFirstLoadMessage = true;
                long now = System.currentTimeMillis();
                Message newMessage = new Message(messageId, myId, friendId, messageContent, replyObject, 0, 0, 0, now, now);
                // inset to firebase
                messagesReference.child(messageId).setValue(newMessage)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // send a notification to FCM
                                Data data = new Data(myName, messageContent, "chat", "messager/" + myId);
                                NotificationExtension.sendFCMNotification(friendId, data);
                            }
                        });
            }
        }
        // reset value
        etContentMessage.setText("");
        clearReplyMessage();
    }

    private void updateReactMessage(int react, int position) {
        Message message = messages.get(position);
        Map<String, Object> childUpdates = new HashMap<>();
        if (message.getReactType() == react) {
            childUpdates.put("/" + message.getId() + "/reactType", 0);
        } else {
            childUpdates.put("/" + message.getId() + "/reactType", react);
        }
        childUpdates.put("/" + message.getId() + "/updatedAt", System.currentTimeMillis());
        messagesReference.updateChildren(childUpdates);
    }

    private void clearReplyMessage() {
        replyObject.setSenderId(0);
        replyObject.setSenderName("");
        replyObject.setContent("");
        tvReplyUserName.setText("");
        tvReplyMessageContent.setText("");
        lInputReplyMessage.setVisibility(View.GONE);
    }
}