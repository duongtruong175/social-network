package vn.hust.socialnetwork.ui.main.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.hawk.Hawk;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.chat.Conversation;
import vn.hust.socialnetwork.models.chat.Message;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.TimeExtension;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private Context context;
    private List<Conversation> conversations;
    private OnConversationListener onConversationListener;
    private DatabaseReference messagesReference;

    public ConversationAdapter(Context context, List<Conversation> conversations, OnConversationListener onConversationListener) {
        this.context = context;
        this.conversations = conversations;
        this.onConversationListener = onConversationListener;
        messagesReference = FirebaseDatabase.getInstance().getReference("messages");
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        Glide.with(context)
                .asBitmap()
                .load(conversation.getReceiver().getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.civImageAvatarConversation);
        holder.tvUserNameConversation.setText(conversation.getReceiver().getName());
        loadLastMessage(conversation.getRoomChatId(), holder.tvLastMessageConversation, holder.tvTimeLastMessageConversation);
    }

    private void loadLastMessage(String roomChatId, AppCompatTextView tvLastMessageConversation, AppCompatTextView tvTimeLastMessageConversation) {
        messagesReference.child(roomChatId).orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    if (message.getSenderId() == Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0)) {
                        tvLastMessageConversation.setText("Bạn: " + message.getContent());
                    } else {
                        tvLastMessageConversation.setText(message.getContent());
                    }
                    tvTimeLastMessageConversation.setText(TimeExtension.formatTimeMessage(message.getCreatedAt()));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public class ConversationViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civImageAvatarConversation;
        AppCompatTextView tvUserNameConversation, tvLastMessageConversation, tvTimeLastMessageConversation;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civImageAvatarConversation = itemView.findViewById(R.id.civ_image_avatar_conversation);
            tvUserNameConversation = itemView.findViewById(R.id.tv_user_name_conversation);
            tvLastMessageConversation = itemView.findViewById(R.id.tv_last_message_conversation);
            tvTimeLastMessageConversation = itemView.findViewById(R.id.tv_time_last_message_conversation);

            if (onConversationListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onConversationListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
