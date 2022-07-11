package vn.hust.socialnetwork.ui.relation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.relation.RelationUser;
import vn.hust.socialnetwork.utils.TimeExtension;

public class RelationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int ITEM_ADD_FRIEND = 1;
    private static final int ITEM_FRIEND = 2;
    private static final int ITEM_REQUEST_FRIEND = 3;
    private static final int ITEM_SUGGEST_FRIEND = 4;

    private Context context;
    private List<RelationUser> relations, filterRelations;
    private OnFriendListener onFriendListener;
    private OnAddFriendListener onAddFriendListener;
    private OnRequestFriendListener onRequestFriendListener;
    private OnSuggestFriendListener onSuggestFriendListener;

    public RelationAdapter(Context context, List<RelationUser> relations, OnFriendListener onFriendListener, OnAddFriendListener onAddFriendListener, OnRequestFriendListener onRequestFriendListener, OnSuggestFriendListener onSuggestFriendListener) {
        this.context = context;
        this.relations = relations;
        this.filterRelations = relations;
        this.onFriendListener = onFriendListener;
        this.onAddFriendListener = onAddFriendListener;
        this.onRequestFriendListener = onRequestFriendListener;
        this.onSuggestFriendListener = onSuggestFriendListener;
    }

    @Override
    public int getItemViewType(int position) {
        RelationUser relation = filterRelations.get(position);
        switch (relation.getRelation()) {
            case "receiver":
                return ITEM_ADD_FRIEND;
            case "friend":
                return ITEM_FRIEND;
            case "sender":
                return ITEM_REQUEST_FRIEND;
            default:
                return ITEM_SUGGEST_FRIEND;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_ADD_FRIEND) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fz_add_friend, parent, false);
            return new AddFriendViewHolder(view);
        } else if (viewType == ITEM_FRIEND) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fz_friend, parent, false);
            return new FriendViewHolder(view);
        } else if (viewType == ITEM_REQUEST_FRIEND) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fz_request_friend, parent, false);
            return new RequestFriendViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fz_suggest_friend, parent, false);
            return new SuggestFriendViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_ADD_FRIEND) {
            AddFriendViewHolder addFriendHolder = (AddFriendViewHolder) holder;
            RelationUser relation = filterRelations.get(position);
            Glide.with(context)
                    .asBitmap()
                    .load(relation.getUser().getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(addFriendHolder.civAvatar);
            addFriendHolder.tvName.setText(relation.getUser().getName());
            addFriendHolder.tvTime.setText(TimeExtension.formatTimeRelation(relation.getUpdatedAt()));
        } else if (getItemViewType(position) == ITEM_FRIEND) {
            FriendViewHolder friendHolder = (FriendViewHolder) holder;
            RelationUser relation = filterRelations.get(position);
            Glide.with(context)
                    .asBitmap()
                    .load(relation.getUser().getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(friendHolder.civAvatar);
            friendHolder.tvName.setText(relation.getUser().getName());
        } else if (getItemViewType(position) == ITEM_REQUEST_FRIEND) {
            RequestFriendViewHolder requestFriendHolder = (RequestFriendViewHolder) holder;
            RelationUser relation = filterRelations.get(position);
            Glide.with(context)
                    .asBitmap()
                    .load(relation.getUser().getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(requestFriendHolder.civAvatar);
            requestFriendHolder.tvName.setText(relation.getUser().getName());
        } else {
            SuggestFriendViewHolder suggestFriendHolder = (SuggestFriendViewHolder) holder;
            RelationUser relation = filterRelations.get(position);
            Glide.with(context)
                    .asBitmap()
                    .load(relation.getUser().getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(suggestFriendHolder.civAvatar);
            suggestFriendHolder.tvName.setText(relation.getUser().getName());
        }
    }

    @Override
    public int getItemCount() {
        return filterRelations.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String name = constraint.toString().toLowerCase();

                List<RelationUser> filter = new ArrayList<>();
                if (name.isEmpty()) {
                    filter = relations;
                } else {
                    for (RelationUser relation: relations) {
                        String userName = relation.getUser().getName().toLowerCase();
                        if (userName.contains(name)) {
                            filter.add(relation);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filter;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterRelations = (List<RelationUser>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class SuggestFriendViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvName;
        AppCompatButton btnRequest, btnDelete;

        public SuggestFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            btnRequest = itemView.findViewById(R.id.btn_request);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            if (onSuggestFriendListener != null) {
                civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSuggestFriendListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSuggestFriendListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                btnRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSuggestFriendListener.onRequestFriendClick(getBindingAdapterPosition());
                    }
                });
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSuggestFriendListener.onDeleteFriendClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }

    public class AddFriendViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvName, tvTime;
        AppCompatButton btnAccept, btnDelete;

        public AddFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            if (onAddFriendListener != null) {
                civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddFriendListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddFriendListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddFriendListener.onAcceptFriendClick(getBindingAdapterPosition());
                    }
                });
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddFriendListener.onDeleteFriendClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvName;
        AppCompatImageView ivChat;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            ivChat = itemView.findViewById(R.id.iv_chat);

            if (onFriendListener != null) {
                civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFriendListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFriendListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                ivChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFriendListener.onChatClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }

    public class RequestFriendViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvName;
        AppCompatButton btnCancel;

        public RequestFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            btnCancel = itemView.findViewById(R.id.btn_cancel);

            if (onRequestFriendListener != null) {
                civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRequestFriendListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRequestFriendListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRequestFriendListener.onCancelFriendClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
