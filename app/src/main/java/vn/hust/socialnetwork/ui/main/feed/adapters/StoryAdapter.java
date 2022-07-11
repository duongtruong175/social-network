package vn.hust.socialnetwork.ui.main.feed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.story.Story;
import vn.hust.socialnetwork.utils.AppSharedPreferences;

public class StoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ADD_STORY_TYPE = 1;
    private static final int STORY_TYPE = 2;

    private Context context;
    private List<Story> stories;
    private OnStoryListener onStoryListener;

    public StoryAdapter(Context context, List<Story> stories, OnStoryListener onStoryListener) {
        this.context = context;
        this.stories = stories;
        this.onStoryListener = onStoryListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ADD_STORY_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_story, parent, false);
            return new AddStoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story, parent, false);
            return new StoryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ADD_STORY_TYPE) {
            String avatar = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "");
            Glide.with(context)
                    .asBitmap()
                    .load(avatar)
                    .error(R.drawable.default_avatar)
                    .into(((AddStoryViewHolder)holder).ivMyAvatar);
        } else {
            // because recycle view display (addStory + stories)
            // story position = (position of item in recycle view) - 1
            Story story = this.stories.get(position - 1);
            ((StoryViewHolder)holder).tvName.setText(story.getUser().getAvatar());
            Glide.with(context)
                    .asBitmap()
                    .load(story.getMedia().getSrc())
                    .into(((StoryViewHolder)holder).ivImageStory);
            Glide.with(context)
                    .asBitmap()
                    .load(story.getUser().getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(((StoryViewHolder)holder).civAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return this.stories.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ADD_STORY_TYPE;
        }
        return STORY_TYPE;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return -1;
        }
        return this.stories.get(position - 1).getId();
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivImageStory;
        CircleImageView civAvatar;
        AppCompatTextView tvName;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            ivImageStory = itemView.findViewById(R.id.iv_image_story);
            civAvatar = itemView.findViewById(R.id.civ_avatar);
            tvName = itemView.findViewById(R.id.tv_name);

            if (onStoryListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStoryListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }
    }

    public class AddStoryViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView ivMyAvatar;
        ProgressBar pbLoadingAddStory;

        public AddStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // binding
            ivMyAvatar = itemView.findViewById(R.id.iv_my_avatar);
            pbLoadingAddStory = itemView.findViewById(R.id.pb_loading_add_story);

            if (onStoryListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onStoryListener.onAddStory();
                    }
                });
            }
        }
    }
}
