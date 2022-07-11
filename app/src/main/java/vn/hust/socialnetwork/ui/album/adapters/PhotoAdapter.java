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

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private Context context;
    private List<String> photos;
    private OnPhotoListener onPhotoListener;

    public PhotoAdapter(Context context, List<String> photos, OnPhotoListener onPhotoListener) {
        this.context = context;
        this.photos = photos;
        this.onPhotoListener = onPhotoListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String photo_url = photos.get(position);
        Glide.with(context)
                .asBitmap()
                .load(photo_url)
                .into(holder.ivPhoto);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            ivPhoto = itemView.findViewById(R.id.iv_photo);

            if (onPhotoListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPhotoListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }
}
