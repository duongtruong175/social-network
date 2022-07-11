package vn.hust.socialnetwork.ui.main.userprofile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.user.Relation;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder>{

    private Context context;
    private List<Relation> friends;
    private OnFriendListener onFriendListener;

    public FriendAdapter(Context context, List<Relation> friends, OnFriendListener onFriendListener) {
        this.context = context;
        this.friends = friends;
        this.onFriendListener = onFriendListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_user_profile_vertical, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Relation friend = friends.get(position);
        Glide.with(context)
                .asBitmap()
                .load(friend.getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.ivImageAvatarFriend);
        holder.tvNameFriend.setText(friend.getName());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivImageAvatarFriend;
        AppCompatTextView tvNameFriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            ivImageAvatarFriend = itemView.findViewById(R.id.iv_image_avatar_friend);
            tvNameFriend = itemView.findViewById(R.id.tv_name_friend);

            if (onFriendListener!= null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFriendListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
