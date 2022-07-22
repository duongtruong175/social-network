package vn.hust.socialnetwork.ui.groupdetail.requestjoin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.group.MemberGroup;
import vn.hust.socialnetwork.utils.TimeExtension;

public class RequestJoinGroupAdapter extends RecyclerView.Adapter<RequestJoinGroupAdapter.RequestJoinGroupViewHolder> implements Filterable {

    private Context context;
    private List<MemberGroup> requestJoins, filterRequestJoins;
    private OnRequestJoinGroupListener onRequestJoinGroupListener;

    public RequestJoinGroupAdapter(Context context, List<MemberGroup> requestJoins, OnRequestJoinGroupListener onRequestJoinGroupListener) {
        this.context = context;
        this.requestJoins = requestJoins;
        this.filterRequestJoins = requestJoins;
        this.onRequestJoinGroupListener = onRequestJoinGroupListener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String name = constraint.toString().toLowerCase();

                List<MemberGroup> filter = new ArrayList<>();
                if (name.isEmpty()) {
                    filter = requestJoins;
                } else {
                    for (MemberGroup requestJoin : requestJoins) {
                        String userName = requestJoin.getUser().getName().toLowerCase();
                        if (userName.contains(name)) {
                            filter.add(requestJoin);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filter;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterRequestJoins = (List<MemberGroup>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public RequestJoinGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_join_group, parent, false);
        return new RequestJoinGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestJoinGroupViewHolder holder, int position) {
        MemberGroup requestJoin = filterRequestJoins.get(position);
        Glide.with(context)
                .asBitmap()
                .load(requestJoin.getUser().getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.civAvatar);
        holder.tvName.setText(requestJoin.getUser().getName());
        holder.tvTime.setText(TimeExtension.formatTimeRelation(requestJoin.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return filterRequestJoins.size();
    }

    public class RequestJoinGroupViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvName, tvTime;
        AppCompatButton btnAccept, btnRefuse;

        public RequestJoinGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnRefuse = itemView.findViewById(R.id.btn_refuse);

            if (onRequestJoinGroupListener != null) {
                civAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRequestJoinGroupListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRequestJoinGroupListener.onUserClick(getBindingAdapterPosition());
                    }
                });
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRequestJoinGroupListener.onAcceptRequestClick(getBindingAdapterPosition());
                    }
                });
                btnRefuse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRequestJoinGroupListener.onRefuseRequestClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
