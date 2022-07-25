package vn.hust.socialnetwork.ui.search.adapters;

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
import vn.hust.socialnetwork.models.user.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private Context context;
    private List<User> users;
    private OnUserListener onUserListener;

    public UserAdapter(Context context, List<User> users, OnUserListener onUserListener) {
        this.context = context;
        this.users = users;
        this.onUserListener = onUserListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_user_profile_vertical, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        Glide.with(context)
                .asBitmap()
                .load(user.getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.ivImageAvatarFriend);
        holder.tvNameFriend.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivImageAvatarFriend;
        AppCompatTextView tvNameFriend;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            ivImageAvatarFriend = itemView.findViewById(R.id.iv_image_avatar_friend);
            tvNameFriend = itemView.findViewById(R.id.tv_name_friend);

            if (onUserListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onUserListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
