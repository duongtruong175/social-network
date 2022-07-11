package vn.hust.socialnetwork.common.view.reactuser.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.post.ReactUser;

public class ReactUserAdapter extends RecyclerView.Adapter<ReactUserAdapter.ReactUserViewHolder> {

    private Context context;
    private List<ReactUser> reactUsers;
    private OnReactUserListener onReactUserListener;

    public ReactUserAdapter(Context context, List<ReactUser> reactUsers, OnReactUserListener onReactUserListener) {
        this.context = context;
        this.reactUsers = reactUsers;
        this.onReactUserListener = onReactUserListener;
    }

    @NonNull
    @Override
    public ReactUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_react_user, parent, false);
        return new ReactUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReactUserViewHolder holder, int position) {
        ReactUser reactUser = reactUsers.get(position);
        Glide.with(context)
                .asBitmap()
                .load(reactUser.getUser().getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.civAvatar);
        switch (reactUser.getType()) {
            case 1:
                holder.civReactType.setImageResource(R.drawable.ic_like);
                break;
            case 2:
                holder.civReactType.setImageResource(R.drawable.ic_heart);
                break;
            case 3:
                holder.civReactType.setImageResource(R.drawable.ic_haha);
                break;
            case 4:
                holder.civReactType.setImageResource(R.drawable.ic_wow);
                break;
            case 5:
                holder.civReactType.setImageResource(R.drawable.ic_sad);
                break;
            case 6:
                holder.civReactType.setImageResource(R.drawable.ic_angry);
                break;
        }
        holder.tvName.setText(reactUser.getUser().getName());
    }

    @Override
    public int getItemCount() {
        return reactUsers.size();
    }

    public class ReactUserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar, civReactType;
        AppCompatTextView tvName;

        public ReactUserViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar =itemView.findViewById(R.id.civ_avatar);
            civReactType = itemView.findViewById(R.id.civ_react_type);
            tvName = itemView.findViewById(R.id.tv_name);

            if (onReactUserListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onReactUserListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
