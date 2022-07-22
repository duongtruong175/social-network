package vn.hust.socialnetwork.ui.message.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.orhanobut.hawk.Hawk;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.jvm.functions.Function1;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.style.CustomTypefaceSpan;
import vn.hust.socialnetwork.models.chat.Message;
import vn.hust.socialnetwork.models.chat.ReplyToMessage;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.TimeExtension;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MESSAGE_LEFT_TYPE = 1;
    private static final int MESSAGE_RIGHT_TYPE = 2;

    private Context context;
    private User friend;
    private List<Message> messages;
    private OnMessageListener onMessageListener;
    private int meId;

    public MessageAdapter(Context context, User friend, List<Message> messages, OnMessageListener onMessageListener) {
        this.context = context;
        this.friend = friend;
        this.messages = messages;
        this.onMessageListener = onMessageListener;
        meId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_LEFT_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
            return new MessageLeftViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
        return new MessageRightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == MESSAGE_LEFT_TYPE) {
            Message message = messages.get(position);
            MessageLeftViewHolder leftViewHolder = (MessageLeftViewHolder) holder;
            Glide.with(context)
                    .asBitmap()
                    .load(friend.getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(leftViewHolder.civAvatar);
            leftViewHolder.tvTimeSendMessage.setText(TimeExtension.formatTimeMessage(message.getCreatedAt()));
            leftViewHolder.tvMessageContent.setText(message.getContent());
            if (message.getReactType() == 0) {
                leftViewHolder.lReactMessage.setVisibility(View.GONE);
            } else {
                leftViewHolder.lReactMessage.setVisibility(View.VISIBLE);
                switch (message.getReactType()) {
                    case 1:
                        leftViewHolder.ivReactMessage.setImageResource(R.drawable.ic_like_action_active);
                        break;
                    case 2:
                        leftViewHolder.ivReactMessage.setImageResource(R.drawable.ic_heart);
                        break;
                    case 3:
                        leftViewHolder.ivReactMessage.setImageResource(R.drawable.ic_haha);
                        break;
                    case 4:
                        leftViewHolder.ivReactMessage.setImageResource(R.drawable.ic_wow);
                        break;
                    case 5:
                        leftViewHolder.ivReactMessage.setImageResource(R.drawable.ic_sad);
                        break;
                    case 6:
                        leftViewHolder.ivReactMessage.setImageResource(R.drawable.ic_angry);
                        break;
                }
            }
            // reply
            ReplyToMessage replyToMessage = message.getReplyToMessage();
            if (replyToMessage != null && replyToMessage.getSenderId() != 0) {
                leftViewHolder.lReply.setVisibility(View.VISIBLE);
                Typeface typeface = ResourcesCompat.getFont(context, R.font.f_roboto_medium);
                if (replyToMessage.getSenderId() == message.getSenderId()) {
                    String nameReply = friend.getName().substring(friend.getName().lastIndexOf(" ") + 1);;
                    SpannableStringBuilder text = new SpannableStringBuilder();
                    text.append(nameReply);
                    text.setSpan(new CustomTypefaceSpan(typeface), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text.append(" đã trả lời chính mình");
                    leftViewHolder.tvReplyName.setText(text);
                } else {
                    String nameReply = friend.getName().substring(friend.getName().lastIndexOf(" ") + 1);;
                    SpannableStringBuilder text = new SpannableStringBuilder();
                    text.append(nameReply);
                    text.setSpan(new CustomTypefaceSpan(typeface), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text.append(" đã trả lời bạn");
                    leftViewHolder.tvReplyName.setText(text);
                }
                leftViewHolder.tvReplyMessage.setText(replyToMessage.getContent());
            } else {
                leftViewHolder.lReply.setVisibility(View.GONE);
            }
        } else {
            Message message = messages.get(position);
            MessageRightViewHolder rightViewHolder = (MessageRightViewHolder) holder;
            Glide.with(context)
                    .asBitmap()
                    .load(friend)
                    .error(R.drawable.default_avatar)
                    .into(rightViewHolder.civAvatar);
            rightViewHolder.tvTimeSendMessage.setText(TimeExtension.formatTimeMessage(message.getCreatedAt()));
            rightViewHolder.tvMessageContent.setText(message.getContent());
            if (message.getReactType() == 0) {
                rightViewHolder.lReactMessage.setVisibility(View.GONE);
            } else {
                rightViewHolder.lReactMessage.setVisibility(View.VISIBLE);
                switch (message.getReactType()) {
                    case 1:
                        rightViewHolder.ivReactMessage.setImageResource(R.drawable.ic_like_action_active);
                        break;
                    case 2:
                        rightViewHolder.ivReactMessage.setImageResource(R.drawable.ic_heart);
                        break;
                    case 3:
                        rightViewHolder.ivReactMessage.setImageResource(R.drawable.ic_haha);
                        break;
                    case 4:
                        rightViewHolder.ivReactMessage.setImageResource(R.drawable.ic_wow);
                        break;
                    case 5:
                        rightViewHolder.ivReactMessage.setImageResource(R.drawable.ic_sad);
                        break;
                    case 6:
                        rightViewHolder.ivReactMessage.setImageResource(R.drawable.ic_angry);
                        break;
                }
            }
            // reply
            ReplyToMessage replyToMessage = message.getReplyToMessage();
            if (replyToMessage != null && replyToMessage.getSenderId() != 0) {
                rightViewHolder.lReply.setVisibility(View.VISIBLE);
                Typeface typeface = ResourcesCompat.getFont(context, R.font.f_roboto_medium);
                if (replyToMessage.getSenderId() == message.getSenderId()) {
                    SpannableStringBuilder text = new SpannableStringBuilder();
                    text.append("Bạn");
                    text.setSpan(new CustomTypefaceSpan(typeface), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text.append(" đã trả lời bạn chính mình");
                    rightViewHolder.tvReplyName.setText(text);
                } else {
                    SpannableStringBuilder text = new SpannableStringBuilder();
                    text.append("Bạn đã trả lời ");
                    int index = text.length();
                    text.append(replyToMessage.getSenderName());
                    text.setSpan(new CustomTypefaceSpan(typeface), index, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    rightViewHolder.tvReplyName.setText(text);
                }
                rightViewHolder.tvReplyMessage.setText(replyToMessage.getContent());
            } else {
                rightViewHolder.lReply.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId() != meId) {
            return MESSAGE_LEFT_TYPE;
        }
        return MESSAGE_RIGHT_TYPE;
    }

    public class MessageLeftViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvTimeSendMessage, tvReplyName, tvReplyMessage, tvMessageContent;
        ConstraintLayout lMessage, lReply;
        CardView lReactMessage;
        AppCompatImageView ivReactMessage;

        public MessageLeftViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvTimeSendMessage = itemView.findViewById(R.id.tv_time_send_message);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            tvReplyName = itemView.findViewById(R.id.tv_reply_name);
            tvReplyMessage = itemView.findViewById(R.id.tv_reply_message);
            lMessage = itemView.findViewById(R.id.l_message);
            lReply = itemView.findViewById(R.id.l_reply);
            lReactMessage = itemView.findViewById(R.id.l_react_message);
            ivReactMessage = itemView.findViewById(R.id.iv_react_message);

            if (onMessageListener != null) {
                civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMessageListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                tvMessageContent.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        tvMessageContent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v1) {
                                int isShow = tvTimeSendMessage.getVisibility();
                                tvTimeSendMessage.setVisibility(isShow == View.VISIBLE ? View.GONE : View.VISIBLE);
                            }
                        });
                        tvMessageContent.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v2) {
                                ReactionPopup reactionPopup = ContextExtension.createReactPostPopup(context);
                                reactionPopup.setReactionSelectedListener(new Function1<Integer, Boolean>() {
                                    @Override
                                    public Boolean invoke(Integer integer) {
                                        onMessageListener.onLongClickContent(integer + 1, getBindingAdapterPosition());
                                        return true;
                                    }
                                });
                                reactionPopup.onTouch(v, event);
                                return false;
                            }
                        });
                        return false;
                    }
                });
            }
        }
    }

    public class MessageRightViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvTimeSendMessage, tvReplyName, tvReplyMessage, tvMessageContent;
        ConstraintLayout lMessage, lReply;
        CardView lReactMessage;
        AppCompatImageView ivReactMessage;

        public MessageRightViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvTimeSendMessage = itemView.findViewById(R.id.tv_time_send_message);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            tvReplyName = itemView.findViewById(R.id.tv_reply_name);
            tvReplyMessage = itemView.findViewById(R.id.tv_reply_message);
            lMessage = itemView.findViewById(R.id.l_message);
            lReply = itemView.findViewById(R.id.l_reply);
            lReactMessage = itemView.findViewById(R.id.l_react_message);
            ivReactMessage = itemView.findViewById(R.id.iv_react_message);

            if (onMessageListener != null) {
                tvMessageContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int isShow = tvTimeSendMessage.getVisibility();
                        tvTimeSendMessage.setVisibility(isShow == View.VISIBLE ? View.GONE : View.VISIBLE);
                    }
                });
            }
        }
    }
}
