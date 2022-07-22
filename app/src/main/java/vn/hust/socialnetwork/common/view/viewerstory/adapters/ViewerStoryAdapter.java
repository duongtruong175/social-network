package vn.hust.socialnetwork.common.view.viewerstory.adapters;

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
import vn.hust.socialnetwork.models.story.ViewerStory;

public class ViewerStoryAdapter extends RecyclerView.Adapter<ViewerStoryAdapter.ViewerStoryViewHolder> {

    private Context context;
    private List<ViewerStory> viewers;
    private OnViewerStoryListener onViewerStoryListener;

    public ViewerStoryAdapter(Context context, List<ViewerStory> viewers, OnViewerStoryListener onViewerStoryListener) {
        this.context = context;
        this.viewers = viewers;
        this.onViewerStoryListener = onViewerStoryListener;
    }

    @NonNull
    @Override
    public ViewerStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_viewer_story, parent, false);
        return new ViewerStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewerStoryViewHolder holder, int position) {
        ViewerStory viewer = viewers.get(position);
        holder.tvName.setText(viewer.getUser().getName());
        Glide.with(context)
                .asBitmap()
                .load(viewer.getUser().getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.civAvatar);
    }

    @Override
    public int getItemCount() {
        return viewers.size();
    }

    public class ViewerStoryViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatar;
        AppCompatTextView tvName;

        public ViewerStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            civAvatar =itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);

            if (onViewerStoryListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onViewerStoryListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
