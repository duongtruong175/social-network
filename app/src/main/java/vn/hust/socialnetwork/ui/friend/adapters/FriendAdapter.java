package vn.hust.socialnetwork.ui.friend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.user.Relation;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> implements Filterable {

    private Context context;
    private List<Relation> friends, filterFriends;
    private OnFriendListener onFriendListener;

    public FriendAdapter(Context context, List<Relation> friends, OnFriendListener onFriendListener) {
        this.context = context;
        this.friends = friends;
        this.filterFriends = friends;
        this.onFriendListener = onFriendListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_in_user_detail, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Relation friend = filterFriends.get(position);
        Glide.with(context)
                .asBitmap()
                .load(friend.getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.civAvatar);
        holder.tvName.setText(friend.getName());
    }

    @Override
    public int getItemCount() {
        return filterFriends.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String name = constraint.toString().toLowerCase();

                List<Relation> filter = new ArrayList<>();
                if (name.isEmpty()) {
                    filter = friends;
                } else {
                    for (Relation friend: friends) {
                        String userName = friend.getName().toLowerCase();
                        if (userName.contains(name)) {
                            filter.add(friend);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filter;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterFriends = (List<Relation>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvName;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);

            if (onFriendListener != null) {
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
