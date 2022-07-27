package vn.hust.socialnetwork.ui.groupdetail.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.pgreze.reactions.ReactionPopup;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.jvm.functions.Function1;
import kr.co.prnd.readmore.ReadMoreTextView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactCount;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.StringExtension;
import vn.hust.socialnetwork.utils.TimeExtension;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Pair<Integer, ExoPlayer> currentPlayingVideo = null;
    private HashMap<Integer, ExoPlayer> playersMap = new HashMap<>();
    private Context context;
    private List<Post> posts;
    private OnPostListener onPostListener;

    public PostAdapter(Context context, List<Post> posts, OnPostListener onPostListener) {
        this.context = context;
        this.posts = posts;
        this.onPostListener = onPostListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        Glide.with(context)
                .asBitmap()
                .load(post.getUser().getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.civUserAvatar);
        holder.tvUserName.setText(post.getUser().getName());
        holder.tvGroupName.setVisibility(View.GONE);
        holder.tvTimePost.setText(TimeExtension.formatTimePost(post.getUpdatedAt()));
        if (post.getCreatedAt().equals(post.getUpdatedAt())) {
            holder.lEdited.setVisibility(View.GONE);
        } else {
            holder.lEdited.setVisibility(View.VISIBLE);
        }
        holder.tvContent.setText(StringExtension.cleanContent(post.getCaption()));
        Media media = post.getMedia();
        if (media != null) {
            holder.lMediaContent.setVisibility(View.VISIBLE);
            if (media.getType().equals(FileExtension.MEDIA_IMAGE_TYPE)) {
                holder.ivImageViewer.setVisibility(View.VISIBLE);
                holder.pvVideoViewer.setVisibility(View.GONE);
                holder.ivVideoThumbnail.setVisibility(View.GONE);
                holder.pbLoadingVideo.setVisibility(View.GONE);
                Glide.with(context)
                        .asBitmap()
                        .load(media.getSrc())
                        .into(holder.ivImageViewer);
            }
            if (media.getType().equals(FileExtension.MEDIA_VIDEO_TYPE)) {
                holder.ivImageViewer.setVisibility(View.GONE);
                holder.pvVideoViewer.setVisibility(View.GONE);
                holder.ivVideoThumbnail.setVisibility(View.VISIBLE);
                holder.pbLoadingVideo.setVisibility(View.VISIBLE);
                // video preview
                RequestOptions options = new RequestOptions().frame(1000);
                Glide.with(context)
                        .load(media.getSrc())
                        .apply(options)
                        .into(holder.ivVideoThumbnail);
                // load video
                releaseRecycledPlayer(position);
                holder.initializePlayer(position, media.getSrc());
            }
        } else {
            holder.lMediaContent.setVisibility(View.GONE);
        }
        // handler react count
        ReactCount reactCount = post.getReactCount();
        int reactType1 = reactCount.getReactType1();
        int reactType2 = reactCount.getReactType2();
        int reactType3 = reactCount.getReactType3();
        int reactType4 = reactCount.getReactType4();
        int reactType5 = reactCount.getReactType5();
        int reactType6 = reactCount.getReactType6();
        if (post.getCounts().getReactCount() == 0) {
            holder.ivSecondReact.setVisibility(View.GONE);
            holder.ivFirstReact.setVisibility(View.VISIBLE);
            holder.ivFirstReact.setImageResource(R.drawable.ic_like);
        } else {
            List<Pair<Integer, Integer>> reactList = new ArrayList<>();
            reactList.add(new Pair<>(reactType1, R.drawable.ic_like));
            reactList.add(new Pair<>(reactType2, R.drawable.ic_heart));
            reactList.add(new Pair<>(reactType3, R.drawable.ic_haha));
            reactList.add(new Pair<>(reactType4, R.drawable.ic_wow));
            reactList.add(new Pair<>(reactType5, R.drawable.ic_sad));
            reactList.add(new Pair<>(reactType6, R.drawable.ic_angry));
            reactList.sort(new Comparator<Pair<Integer, Integer>>() {
                @Override
                public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                    if (o2.first > o1.first) {
                        return 1;
                    } else if (o2.first.equals(o1.first)) {
                        return 0;
                    }
                    return -1;
                }
            });
            if (reactList.get(0).first != 0) {
                holder.ivFirstReact.setVisibility(View.VISIBLE);
                holder.ivFirstReact.setImageResource(reactList.get(0).second);
            } else {
                holder.ivFirstReact.setVisibility(View.GONE);
            }
            if (reactList.get(1).first != 0) {
                holder.ivSecondReact.setVisibility(View.VISIBLE);
                holder.ivSecondReact.setImageResource(reactList.get(1).second);
            } else {
                holder.ivSecondReact.setVisibility(View.GONE);
            }
        }
        holder.tvReactCount.setText(StringExtension.formatReactCountPost(post.getCounts().getReactCount()));
        holder.tvCommentCount.setText(post.getCounts().getCommentCount() + " " + context.getString(R.string.comment_count));
        switch (post.getReactStatus()) {
            // logged in user don't react with this post
            case 1:
                holder.ivReactAction.setImageResource(R.drawable.ic_like_action_active);
                holder.tvReactAction.setText(R.string.like);
                holder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_like));
                break;
            case 2:
                holder.ivReactAction.setImageResource(R.drawable.ic_heart);
                holder.tvReactAction.setText(R.string.heart);
                holder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_heart));
                break;
            case 3:
                holder.ivReactAction.setImageResource(R.drawable.ic_haha);
                holder.tvReactAction.setText(R.string.haha);
                holder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_haha));
                break;
            case 4:
                holder.ivReactAction.setImageResource(R.drawable.ic_wow);
                holder.tvReactAction.setText(R.string.wow);
                holder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_wow));
                break;
            case 5:
                holder.ivReactAction.setImageResource(R.drawable.ic_sad);
                holder.tvReactAction.setText(R.string.sad);
                holder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_sad));
                break;
            case 6:
                holder.ivReactAction.setImageResource(R.drawable.ic_angry);
                holder.tvReactAction.setText(R.string.angry);
                holder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_angry));
                break;
            default:
                holder.ivReactAction.setImageResource(R.drawable.ic_like_action);
                holder.tvReactAction.setText(R.string.like);
                holder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_drawable_post));
                break;
        }
        Glide.with(context)
                .asBitmap()
                .load(Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, ""))
                .error(R.drawable.default_avatar)
                .into(holder.civMyAvatar);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civUserAvatar, civMyAvatar;
        AppCompatTextView tvUserName, tvGroupName, tvTimePost, tvReactCount, tvCommentCount, tvReactAction;
        AppCompatImageView ivMenu, ivSecondReact, ivFirstReact, ivReactAction;
        ReadMoreTextView tvContent;
        ConstraintLayout lContentToShare, lMediaContent, lComment, lReactCount, lEdited, lHeader, lReact;
        AppCompatImageView ivImageViewer, ivVideoThumbnail;
        ProgressBar pbLoadingVideo;
        PlayerView pvVideoViewer;
        ExoPlayer exoPlayer; // controller video
        AppCompatImageButton btnVolume;
        boolean isMute;
        LinearLayoutCompat lReactAction, lCommentAction, lShareAction;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // init
            isMute = false;

            // binding
            civUserAvatar = itemView.findViewById(R.id.civ_user_avatar);
            civMyAvatar = itemView.findViewById(R.id.civ_my_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvTimePost = itemView.findViewById(R.id.tv_time_post);
            lEdited = itemView.findViewById(R.id.l_edited);
            ivMenu = itemView.findViewById(R.id.iv_menu);
            ivSecondReact = itemView.findViewById(R.id.iv_second_react);
            ivFirstReact = itemView.findViewById(R.id.iv_first_react);
            ivReactAction = itemView.findViewById(R.id.iv_react_action);
            tvReactAction = itemView.findViewById(R.id.tv_react_action);
            tvContent = itemView.findViewById(R.id.tv_content);
            lHeader = itemView.findViewById(R.id.l_header);
            lReact = itemView.findViewById(R.id.l_react);
            lReactCount = itemView.findViewById(R.id.l_react_count);
            tvReactCount = itemView.findViewById(R.id.tv_react_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            lMediaContent = itemView.findViewById(R.id.l_media_content);
            lComment = itemView.findViewById(R.id.l_comment);
            ivImageViewer = itemView.findViewById(R.id.iv_image_viewer);
            pvVideoViewer = itemView.findViewById(R.id.pv_video_viewer);
            ivVideoThumbnail = itemView.findViewById(R.id.iv_video_thumbnail);
            pbLoadingVideo = itemView.findViewById(R.id.pb_loading_video);
            btnVolume = pvVideoViewer.findViewById(R.id.btn_volume);
            lReactAction = itemView.findViewById(R.id.l_react_action);
            lCommentAction = itemView.findViewById(R.id.l_comment_action);
            lShareAction = itemView.findViewById(R.id.l_share_action);
            lContentToShare = itemView.findViewById(R.id.l_content_to_share);

            if (onPostListener != null) {
                civUserAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onUserPostClick(getBindingAdapterPosition());
                    }
                });
                tvUserName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onUserPostClick(getBindingAdapterPosition());
                    }
                });
                ivMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onMenuItemClick(getBindingAdapterPosition());
                    }
                });
                lMediaContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onMediaItemClick(getBindingAdapterPosition());
                    }
                });
                pvVideoViewer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pvVideoViewer.isControllerVisible()) {
                            onPostListener.onMediaItemClick(getBindingAdapterPosition());
                        }
                    }
                });
                tvContent.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onPostListener.onContentLongClick(getBindingAdapterPosition());
                        return false;
                    }
                });
                lReactCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onReactCountClick(getBindingAdapterPosition());
                    }
                });
                lReactAction.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        lReactAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v1) {
                                // calculator total react and createOrDelete react
                                int position = getBindingAdapterPosition();
                                Post post = posts.get(position);
                                int oldType = posts.get(position).getReactStatus();
                                if (oldType == 0) {
                                    onPostListener.onReactActionClick(1, position);
                                    post.setReactStatus(1);
                                    int reactType1Count = post.getReactCount().getReactType1() + 1;
                                    post.getReactCount().setReactType1(reactType1Count);
                                    int count = post.getCounts().getReactCount() + 1;
                                    post.getCounts().setReactCount(count);
                                } else {
                                    onPostListener.onReactActionClick(0, position); // = 0 -> delete
                                    post.setReactStatus(0);
                                    switch (oldType) {
                                        case 1:
                                            int reactType1Count = post.getReactCount().getReactType1() - 1;
                                            post.getReactCount().setReactType1(reactType1Count);
                                            break;
                                        case 2:
                                            int reactType2Count = post.getReactCount().getReactType2() - 1;
                                            post.getReactCount().setReactType2(reactType2Count);
                                            break;
                                        case 3:
                                            int reactType3Count = post.getReactCount().getReactType3() - 1;
                                            post.getReactCount().setReactType3(reactType3Count);
                                            break;
                                        case 4:
                                            int reactType4Count = post.getReactCount().getReactType4() - 1;
                                            post.getReactCount().setReactType4(reactType4Count);
                                            break;
                                        case 5:
                                            int reactType5Count = post.getReactCount().getReactType5() - 1;
                                            post.getReactCount().setReactType5(reactType5Count);
                                            break;
                                        case 6:
                                            int reactType6Count = post.getReactCount().getReactType6() - 1;
                                            post.getReactCount().setReactType6(reactType6Count);
                                            break;
                                    }
                                    int count = post.getCounts().getReactCount() - 1;
                                    post.getCounts().setReactCount(count);
                                }
                                posts.set(position, post);
                                updateReactPost(post);
                            }
                        });
                        lReactAction.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v2) {
                                ReactionPopup reactionPopup = ContextExtension.createReactPostPopup(context);
                                reactionPopup.setReactionSelectedListener(new Function1<Integer, Boolean>() {
                                    @Override
                                    public Boolean invoke(Integer integer) {
                                        if (integer >= 0 && integer <= 5) {
                                            int position = getBindingAdapterPosition();
                                            int reactType = integer + 1;
                                            Post post = posts.get(position);
                                            int oldType = posts.get(position).getReactStatus();
                                            if (oldType != reactType) { // update
                                                onPostListener.onReactActionLongClick(reactType, position);
                                                if (oldType != 0) {
                                                    switch (oldType) {
                                                        case 1:
                                                            int reactType1Count = post.getReactCount().getReactType1() - 1;
                                                            post.getReactCount().setReactType1(reactType1Count);
                                                            break;
                                                        case 2:
                                                            int reactType2Count = post.getReactCount().getReactType2() - 1;
                                                            post.getReactCount().setReactType2(reactType2Count);
                                                            break;
                                                        case 3:
                                                            int reactType3Count = post.getReactCount().getReactType3() - 1;
                                                            post.getReactCount().setReactType3(reactType3Count);
                                                            break;
                                                        case 4:
                                                            int reactType4Count = post.getReactCount().getReactType4() - 1;
                                                            post.getReactCount().setReactType4(reactType4Count);
                                                            break;
                                                        case 5:
                                                            int reactType5Count = post.getReactCount().getReactType5() - 1;
                                                            post.getReactCount().setReactType5(reactType5Count);
                                                            break;
                                                        case 6:
                                                            int reactType6Count = post.getReactCount().getReactType6() - 1;
                                                            post.getReactCount().setReactType6(reactType6Count);
                                                            break;
                                                    }
                                                } else {
                                                    int count = post.getCounts().getReactCount() + 1;
                                                    post.getCounts().setReactCount(count);
                                                }
                                                post.setReactStatus(reactType);
                                                switch (reactType) {
                                                    case 1:
                                                        int reactType1Count = post.getReactCount().getReactType1() + 1;
                                                        post.getReactCount().setReactType1(reactType1Count);
                                                        break;
                                                    case 2:
                                                        int reactType2Count = post.getReactCount().getReactType2() + 1;
                                                        post.getReactCount().setReactType2(reactType2Count);
                                                        break;
                                                    case 3:
                                                        int reactType3Count = post.getReactCount().getReactType3() + 1;
                                                        post.getReactCount().setReactType3(reactType3Count);
                                                        break;
                                                    case 4:
                                                        int reactType4Count = post.getReactCount().getReactType4() + 1;
                                                        post.getReactCount().setReactType4(reactType4Count);
                                                        break;
                                                    case 5:
                                                        int reactType5Count = post.getReactCount().getReactType5() + 1;
                                                        post.getReactCount().setReactType5(reactType5Count);
                                                        break;
                                                    case 6:
                                                        int reactType6Count = post.getReactCount().getReactType6() + 1;
                                                        post.getReactCount().setReactType6(reactType6Count);
                                                        break;
                                                }
                                                posts.set(position, post);
                                                updateReactPost(post);
                                            }
                                        }
                                        return true;
                                    }
                                });
                                reactionPopup.onTouch(v, event);
                                return false;
                            }
                        });
                        return false;
                    }
                });
                lShareAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onShareActionClick(lContentToShare, getBindingAdapterPosition());
                    }
                });
                lCommentAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onCommentActionClick(getBindingAdapterPosition());
                    }
                });
                lComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onShowCommentDialogClick(tvCommentCount, getBindingAdapterPosition());
                    }
                });
                lHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onItemClick(getBindingAdapterPosition());
                    }
                });
                lReact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onItemClick(getBindingAdapterPosition());
                    }
                });
            }
        }

        // update view
        private void updateReactPost(Post post) {
            // handler react count
            ReactCount reactCount = post.getReactCount();
            int reactType1 = reactCount.getReactType1();
            int reactType2 = reactCount.getReactType2();
            int reactType3 = reactCount.getReactType3();
            int reactType4 = reactCount.getReactType4();
            int reactType5 = reactCount.getReactType5();
            int reactType6 = reactCount.getReactType6();
            if (post.getCounts().getReactCount() == 0) {
                ivSecondReact.setVisibility(View.GONE);
                ivFirstReact.setVisibility(View.VISIBLE);
                ivFirstReact.setImageResource(R.drawable.ic_like);
            } else {
                List<Pair<Integer, Integer>> reactList = new ArrayList<>();
                reactList.add(new Pair<>(reactType1, R.drawable.ic_like));
                reactList.add(new Pair<>(reactType2, R.drawable.ic_heart));
                reactList.add(new Pair<>(reactType3, R.drawable.ic_haha));
                reactList.add(new Pair<>(reactType4, R.drawable.ic_wow));
                reactList.add(new Pair<>(reactType5, R.drawable.ic_sad));
                reactList.add(new Pair<>(reactType6, R.drawable.ic_angry));
                reactList.sort(new Comparator<Pair<Integer, Integer>>() {
                    @Override
                    public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                        if (o2.first > o1.first) {
                            return 1;
                        } else if (o2.first.equals(o1.first)) {
                            return 0;
                        }
                        return -1;
                    }
                });
                if (reactList.get(0).first != 0) {
                    ivFirstReact.setVisibility(View.VISIBLE);
                    ivFirstReact.setImageResource(reactList.get(0).second);
                } else {
                    ivFirstReact.setVisibility(View.GONE);
                }
                if (reactList.get(1).first != 0) {
                    ivSecondReact.setVisibility(View.VISIBLE);
                    ivSecondReact.setImageResource(reactList.get(1).second);
                } else {
                    ivSecondReact.setVisibility(View.GONE);
                }
            }
            tvReactCount.setText(StringExtension.formatReactCountPost(post.getCounts().getReactCount()));
            tvCommentCount.setText(post.getCounts().getCommentCount() + " " + context.getString(R.string.comment_count));
            switch (post.getReactStatus()) {
                // logged in user don't react with this post
                case 1:
                    ivReactAction.setImageResource(R.drawable.ic_like_action_active);
                    tvReactAction.setText(R.string.like);
                    tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_like));
                    break;
                case 2:
                    ivReactAction.setImageResource(R.drawable.ic_heart);
                    tvReactAction.setText(R.string.heart);
                    tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_heart));
                    break;
                case 3:
                    ivReactAction.setImageResource(R.drawable.ic_haha);
                    tvReactAction.setText(R.string.haha);
                    tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_haha));
                    break;
                case 4:
                    ivReactAction.setImageResource(R.drawable.ic_wow);
                    tvReactAction.setText(R.string.wow);
                    tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_wow));
                    break;
                case 5:
                    ivReactAction.setImageResource(R.drawable.ic_sad);
                    tvReactAction.setText(R.string.sad);
                    tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_sad));
                    break;
                case 6:
                    ivReactAction.setImageResource(R.drawable.ic_angry);
                    tvReactAction.setText(R.string.angry);
                    tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_angry));
                    break;
                default:
                    ivReactAction.setImageResource(R.drawable.ic_like_action);
                    tvReactAction.setText(R.string.like);
                    tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_drawable_post));
                    break;
            }
        }

        /**
         * initialize the player
         */
        private void initializePlayer(int position, String media_url) {
            // Set up the factory for media sources.
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);

            MediaSource.Factory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory);

            // Create an ExoPlayer and set it as the player for content.
            exoPlayer = new ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build();
            pvVideoViewer.setPlayer(exoPlayer);

            // Create the MediaItem to play, specifying the content URI.
            Uri contentUri = Uri.parse(media_url);
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(contentUri)
                    .build();

            // Prepare the content and ad to be played with the ExoPlayer.
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();

            if (playersMap.containsKey(position)) {
                playersMap.remove(position);
            }
            playersMap.put(position, exoPlayer);

            // Set PlayWhenReady. If true, content will autoplay.
            exoPlayer.setPlayWhenReady(false);

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    if (playbackState == Player.STATE_IDLE) {
                        ivVideoThumbnail.setVisibility(View.VISIBLE);
                        pbLoadingVideo.setVisibility(View.VISIBLE);
                        pvVideoViewer.setVisibility(View.GONE);
                    } else if (playbackState == Player.STATE_READY) {
                        ivVideoThumbnail.setVisibility(View.GONE);
                        pbLoadingVideo.setVisibility(View.GONE);
                        pvVideoViewer.setVisibility(View.VISIBLE);
                    }
                    if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                        pvVideoViewer.setKeepScreenOn(false);
                    } else { // STATE_READY || STATE_BUFFERING
                        pvVideoViewer.setKeepScreenOn(true);
                    }
                }
            });

            btnVolume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isMute = !isMute;
                    if (isMute) {
                        exoPlayer.setVolume(0.0f);
                        btnVolume.setImageResource(R.drawable.ic_volume_off);
                    } else {
                        exoPlayer.setVolume(1.0f);
                        btnVolume.setImageResource(R.drawable.ic_volume_on);
                    }
                }
            });
        }
    }

    // delete all players view
    public void releaseAllPlayers() {
        playersMap.forEach((key, exoPlayer) -> {
            if (exoPlayer != null) {
                exoPlayer.release();
                exoPlayer = null;
            }
        });
    }

    // pause all players
    public void pauseAllPlayers() {
        playersMap.forEach((key, exoPlayer) -> {
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
            }
        });
    }

    // call when item recycled to improve performance
    public void releaseRecycledPlayer(int index) {
        ExoPlayer exoPlayer = playersMap.get(index);
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    // call when scroll to pause any playing player
    public void continueCurrentPlayingVideo() {
        if (currentPlayingVideo != null) {
            ExoPlayer exoPlayer = currentPlayingVideo.second;
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(true);
            }
        }
    }

    // call when scroll to pause any playing player
    public void pauseCurrentPlayingVideo() {
        if (currentPlayingVideo != null) {
            ExoPlayer exoPlayer = currentPlayingVideo.second;
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
            }
        }
    }

    // pause item in position and pause others
    public void pausePlayerVideo(int index) {
        ExoPlayer exoPlayer = playersMap.get(index);
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    // play item in position and pause others
    public void playIndexThenPausePreviousPlayer(int index) {
        ExoPlayer exoPlayer = playersMap.get(index);
        if (exoPlayer != null && !exoPlayer.getPlayWhenReady()) {
            pauseCurrentPlayingVideo();
            exoPlayer.setPlayWhenReady(true);
            currentPlayingVideo = new Pair<>(index, exoPlayer);
        }
    }
}
