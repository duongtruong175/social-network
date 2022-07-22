package vn.hust.socialnetwork.ui.groupdetail.viewmember.adapters;

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
import vn.hust.socialnetwork.models.group.MemberGroup;

public class MemberGroupAdapter extends RecyclerView.Adapter<MemberGroupAdapter.MemberGroupViewHolder> implements Filterable {

    private Context context;
    private List<MemberGroup> memberGroups, filterMemberGroups;
    private OnMemberGroupListener onMemberGroupListener;

    public MemberGroupAdapter(Context context, List<MemberGroup> memberGroups, OnMemberGroupListener onMemberGroupListener) {
        this.context = context;
        this.memberGroups = memberGroups;
        this.filterMemberGroups = memberGroups;
        this.onMemberGroupListener = onMemberGroupListener;
    }

    @NonNull
    @Override
    public MemberGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_group, parent, false);
        return new MemberGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberGroupViewHolder holder, int position) {
        MemberGroup member = filterMemberGroups.get(position);
        Glide.with(context)
                .asBitmap()
                .load(member.getUser().getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.civAvatar);
        holder.tvName.setText(member.getUser().getName());
    }

    @Override
    public int getItemCount() {
        return filterMemberGroups.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String name = constraint.toString().toLowerCase();

                List<MemberGroup> filter = new ArrayList<>();
                if (name.isEmpty()) {
                    filter = memberGroups;
                } else {
                    for (MemberGroup member : memberGroups) {
                        String userName = member.getUser().getName().toLowerCase();
                        if (userName.contains(name)) {
                            filter.add(member);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filter;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterMemberGroups = (List<MemberGroup>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class MemberGroupViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvName;

        public MemberGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);

            if (onMemberGroupListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMemberGroupListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
