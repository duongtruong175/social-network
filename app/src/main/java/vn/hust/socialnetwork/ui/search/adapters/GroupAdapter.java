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
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.utils.StringExtension;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private Context context;
    private List<Group> groups;
    private OnGroupListener onGroupListener;

    public GroupAdapter(Context context, List<Group> groups, OnGroupListener onGroupListener) {
        this.context = context;
        this.groups = groups;
        this.onGroupListener = onGroupListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        Glide.with(context)
                .asBitmap()
                .load(group.getCoverImage())
                .error(R.drawable.default_group_cover)
                .into(holder.ivGroupCover);
        holder.tvGroupName.setText(group.getName());
        holder.tvGroupMemberCount.setText(StringExtension.formatMemberGroupCount(group.getCounts().getMemberCount()) + " thành viên");
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivGroupCover;
        AppCompatTextView tvGroupName, tvGroupMemberCount;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            ivGroupCover = itemView.findViewById(R.id.iv_group_cover);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvGroupMemberCount = itemView.findViewById(R.id.tv_group_member_count);

            if (onGroupListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onGroupListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
