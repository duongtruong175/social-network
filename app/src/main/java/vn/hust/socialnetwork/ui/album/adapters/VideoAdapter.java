package vn.hust.socialnetwork.ui.album.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.hust.socialnetwork.R;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<String> videos;
    private OnVideoListener onVideoListener;

    public VideoAdapter(Context context, List<String> videos, OnVideoListener onVideoListener) {
        this.context = context;
        this.videos = videos;
        this.onVideoListener = onVideoListener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        String video_url = videos.get(position);
        Glide.with(context)
                .asBitmap()
                .load(video_url)
                .into(holder.ivVideoPhoto);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivVideoPhoto;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            ivVideoPhoto = itemView.findViewById(R.id.iv_video_photo);

            if (onVideoListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onVideoListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
