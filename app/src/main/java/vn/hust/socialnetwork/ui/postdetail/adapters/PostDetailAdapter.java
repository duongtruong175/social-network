package vn.hust.socialnetwork.ui.postdetail.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.pgreze.reactions.ReactionPopup;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.jvm.functions.Function1;
import kr.co.prnd.readmore.ReadMoreTextView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.CommentPost;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactCount;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.StringExtension;
import vn.hust.socialnetwork.utils.TimeExtension;

public class PostDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_POST_DETAIL = 1;
    private static final int ITEM_COMMENT = 2;

    private Context context;
    private List<Post> posts; // only one item
    private OnPostListener onPostListener;
    private List<CommentPost> comments;
    private OnCommentListener onCommentListener;
    private ExoPlayer currentPlayingVideo = null;

    public PostDetailAdapter(Context context, List<Post> posts, OnPostListener onPostListener, List<CommentPost> comments, OnCommentListener onCommentListener) {
        this.context = context;
        this.posts = posts;
        this.onPostListener = onPostListener;
        this.comments = comments;
        this.onCommentListener = onCommentListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_POST_DETAIL;
        }
        return ITEM_COMMENT;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof PostViewHolder) {
            continuePlayerVideo();
        }
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof PostViewHolder) {
            pausePlayerVideo();
        }
        super.onViewDetachedFromWindow(holder);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_POST_DETAIL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_in_detail, parent, false);
            return new PostViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_POST_DETAIL) {
            Post post = posts.get(0);
            PostViewHolder postHolder = (PostViewHolder) holder;
            Glide.with(context)
                    .asBitmap()
                    .load(post.getUser().getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(postHolder.civUserAvatar);
            postHolder.tvUserName.setText(post.getUser().getName());
            if (post.getGroup() != null) {
                postHolder.tvGroupName.setVisibility(View.VISIBLE);
                postHolder.tvGroupName.setText(post.getGroup().getName());
            } else {
                postHolder.tvGroupName.setVisibility(View.GONE);
            }
            postHolder.tvTimePost.setText(TimeExtension.formatTimePost(post.getUpdatedAt()));
            if (post.getCreatedAt().equals(post.getUpdatedAt())) {
                postHolder.lEdited.setVisibility(View.GONE);
            } else {
                postHolder.lEdited.setVisibility(View.VISIBLE);
            }
            postHolder.tvContent.setText(post.getCaption());
            Media media = post.getMedia();
            if (media != null) {
                postHolder.lMediaContent.setVisibility(View.VISIBLE);
                if (media.getType().equals(FileExtension.MEDIA_IMAGE_TYPE)) {
                    postHolder.ivImageViewer.setVisibility(View.VISIBLE);
                    postHolder.pvVideoViewer.setVisibility(View.GONE);
                    postHolder.ivVideoThumbnail.setVisibility(View.GONE);
                    Glide.with(context)
                            .asBitmap()
                            .load(media.getSrc())
                            .into(postHolder.ivImageViewer);
                }
                if (media.getType().equals(FileExtension.MEDIA_VIDEO_TYPE)) {
                    postHolder.ivImageViewer.setVisibility(View.GONE);
                    postHolder.pvVideoViewer.setVisibility(View.GONE);
                    postHolder.ivVideoThumbnail.setVisibility(View.VISIBLE);
                    postHolder.pbLoadingVideo.setVisibility(View.VISIBLE);
                    // video preview
                    RequestOptions options = new RequestOptions().frame(1000);
                    Glide.with(context)
                            .load(media.getSrc())
                            .apply(options)
                            .into(postHolder.ivVideoThumbnail);
                    // load video
                    if (currentPlayingVideo != null) {
                        releasePlayer();
                    }
                    postHolder.initializePlayer(media.getSrc());
                }
            } else {
                postHolder.lMediaContent.setVisibility(View.GONE);
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
                postHolder.ivSecondReact.setVisibility(View.GONE);
                postHolder.ivFirstReact.setVisibility(View.VISIBLE);
                postHolder.ivFirstReact.setImageResource(R.drawable.ic_like);
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
                    postHolder.ivFirstReact.setVisibility(View.VISIBLE);
                    postHolder.ivFirstReact.setImageResource(reactList.get(0).second);
                } else {
                    postHolder.ivFirstReact.setVisibility(View.GONE);
                }
                if (reactList.get(1).first != 0) {
                    postHolder.ivSecondReact.setVisibility(View.VISIBLE);
                    postHolder.ivSecondReact.setImageResource(reactList.get(1).second);
                } else {
                    postHolder.ivSecondReact.setVisibility(View.GONE);
                }
            }
            postHolder.tvReactCount.setText(StringExtension.formatReactCountPost(post.getCounts().getReactCount()));
            postHolder.tvCommentCount.setText(post.getCounts().getCommentCount() + " " + context.getString(R.string.comment_count));
            switch (post.getReactStatus()) {
                // logged in user don't react with this post
                case 1:
                    postHolder.ivReactAction.setImageResource(R.drawable.ic_like_action_active);
                    postHolder.tvReactAction.setText(R.string.like);
                    postHolder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_like));
                    break;
                case 2:
                    postHolder.ivReactAction.setImageResource(R.drawable.ic_heart);
                    postHolder.tvReactAction.setText(R.string.heart);
                    postHolder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_heart));
                    break;
                case 3:
                    postHolder.ivReactAction.setImageResource(R.drawable.ic_haha);
                    postHolder.tvReactAction.setText(R.string.haha);
                    postHolder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_haha));
                    break;
                case 4:
                    postHolder.ivReactAction.setImageResource(R.drawable.ic_wow);
                    postHolder.tvReactAction.setText(R.string.wow);
                    postHolder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_wow));
                    break;
                case 5:
                    postHolder.ivReactAction.setImageResource(R.drawable.ic_sad);
                    postHolder.tvReactAction.setText(R.string.sad);
                    postHolder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_sad));
                    break;
                case 6:
                    postHolder.ivReactAction.setImageResource(R.drawable.ic_angry);
                    postHolder.tvReactAction.setText(R.string.angry);
                    postHolder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_text_angry));
                    break;
                default:
                    postHolder.ivReactAction.setImageResource(R.drawable.ic_like_action);
                    postHolder.tvReactAction.setText(R.string.like);
                    postHolder.tvReactAction.setTextColor(ContextCompat.getColor(context, R.color.color_drawable_post));
                    break;
            }
        } else if (getItemViewType(position) == ITEM_COMMENT) {
            CommentViewHolder commentHolder = (CommentViewHolder) holder;
            CommentPost comment = comments.get(position - 1);
            Glide.with(context)
                    .asBitmap()
                    .load(comment.getUser().getAvatar())
                    .error(R.drawable.default_avatar)
                    .into(commentHolder.civImageAvatarComment);
            commentHolder.tvUserNameComment.setText(comment.getUser().getName());
            commentHolder.tvContentComment.setText(comment.getContent());
            commentHolder.tvTimeComment.setText(TimeExtension.formatTimeComment(comment.getUpdatedAt()));
            if (comment.getCreatedAt().equals(comment.getUpdatedAt())) {
                commentHolder.lEdited.setVisibility(View.GONE);
            } else {
                commentHolder.lEdited.setVisibility(View.VISIBLE);
            }
            Media media = comment.getMedia();
            if (media != null) {
                commentHolder.lCommentMedia.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .asBitmap()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .load(media.getSrc())
                        .into(commentHolder.ivImageComment);
            } else {
                commentHolder.lCommentMedia.setVisibility(View.GONE);
            }
            // handler react count
            ReactCount reactCount = comment.getReactCount();
            int reactType1 = reactCount.getReactType1();
            int reactType2 = reactCount.getReactType2();
            int reactType3 = reactCount.getReactType3();
            int reactType4 = reactCount.getReactType4();
            int reactType5 = reactCount.getReactType5();
            int reactType6 = reactCount.getReactType6();
            if (comment.getCounts().getReactCount() == 0) {
                commentHolder.lReactComment.setVisibility(View.GONE);
            } else {
                commentHolder.lReactComment.setVisibility(View.VISIBLE);
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
                    commentHolder.ivFirstReactComment.setVisibility(View.VISIBLE);
                    commentHolder.ivFirstReactComment.setImageResource(reactList.get(0).second);
                } else {
                    commentHolder.ivFirstReactComment.setVisibility(View.GONE);
                }
                if (reactList.get(1).first != 0) {
                    commentHolder.ivSecondReactComment.setVisibility(View.VISIBLE);
                    commentHolder.ivSecondReactComment.setImageResource(reactList.get(1).second);
                } else {
                    commentHolder.ivSecondReactComment.setVisibility(View.GONE);
                }
                commentHolder.tvReactCommentCount.setText(StringExtension.formatReactCountPost(comment.getCounts().getReactCount()));
            }
            switch (comment.getReactStatus()) {
                // logged in user don't react with this post
                case 1:
                    commentHolder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_like));
                    break;
                case 2:
                    commentHolder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_heart));
                    break;
                case 3:
                    commentHolder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_haha));
                    break;
                case 4:
                    commentHolder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_wow));
                    break;
                case 5:
                    commentHolder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_sad));
                    break;
                case 6:
                    commentHolder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_angry));
                    break;
                default:
                    commentHolder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return posts.size() + comments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civImageAvatarComment;
        AppCompatTextView tvUserNameComment, tvReactCommentCount, tvTimeComment, tvReactActionComment, tvReplyComment;
        ReadMoreTextView tvContentComment;
        ConstraintLayout lEdited, lCommentContainer;
        AppCompatImageView ivImageComment, ivSecondReactComment, ivFirstReactComment;
        CardView lReactComment, lCommentMedia;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            //binding
            lCommentContainer = itemView.findViewById(R.id.l_comment_container);
            civImageAvatarComment = itemView.findViewById(R.id.civ_image_avatar_comment);
            tvUserNameComment = itemView.findViewById(R.id.tv_user_name_comment);
            tvReactCommentCount = itemView.findViewById(R.id.tv_react_comment_count);
            lEdited = itemView.findViewById(R.id.l_edited);
            lCommentMedia = itemView.findViewById(R.id.l_comment_media);
            tvTimeComment = itemView.findViewById(R.id.tv_time_comment);
            tvReactActionComment = itemView.findViewById(R.id.tv_react_action_comment);
            tvReplyComment = itemView.findViewById(R.id.tv_reply_comment);
            tvContentComment = itemView.findViewById(R.id.tv_content_comment);
            ivImageComment = itemView.findViewById(R.id.iv_image_comment);
            ivSecondReactComment = itemView.findViewById(R.id.iv_second_react_comment);
            ivFirstReactComment = itemView.findViewById(R.id.iv_first_react_comment);
            lReactComment = itemView.findViewById(R.id.l_react_comment);

            if (onCommentListener != null) {
                civImageAvatarComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onUserCommentClick(getBindingAdapterPosition() - posts.size());
                    }
                });
                tvUserNameComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onUserCommentClick(getBindingAdapterPosition() - posts.size());
                    }
                });
                tvContentComment.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onCommentListener.onItemLongClick(tvUserNameComment, getBindingAdapterPosition() - posts.size());
                        return false;
                    }
                });
                ivImageComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onMediaItemClick(getBindingAdapterPosition() - posts.size());
                    }
                });
                lReactComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onReactCountClick(getBindingAdapterPosition() - posts.size());
                    }
                });
                tvReplyComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onReplyActionClick(getBindingAdapterPosition() - posts.size());
                    }
                });
                tvReactActionComment.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        tvReactActionComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v1) {
                                int position = getBindingAdapterPosition() - posts.size();
                                CommentPost comment = comments.get(position);
                                int oldType = comments.get(position).getReactStatus();
                                if (oldType == 0) {
                                    onCommentListener.onReactActionClick(1, position);
                                    comment.setReactStatus(1);
                                    int reactType1Count = comment.getReactCount().getReactType1() + 1;
                                    comment.getReactCount().setReactType1(reactType1Count);
                                    int count = comment.getCounts().getReactCount() + 1;
                                    comment.getCounts().setReactCount(count);
                                } else {
                                    onCommentListener.onReactActionClick(0, position);
                                    comment.setReactStatus(0);
                                    switch (oldType) {
                                        case 1:
                                            int reactType1Count = comment.getReactCount().getReactType1() - 1;
                                            comment.getReactCount().setReactType1(reactType1Count);
                                            break;
                                        case 2:
                                            int reactType2Count = comment.getReactCount().getReactType2() - 1;
                                            comment.getReactCount().setReactType2(reactType2Count);
                                            break;
                                        case 3:
                                            int reactType3Count = comment.getReactCount().getReactType3() - 1;
                                            comment.getReactCount().setReactType3(reactType3Count);
                                            break;
                                        case 4:
                                            int reactType4Count = comment.getReactCount().getReactType4() - 1;
                                            comment.getReactCount().setReactType4(reactType4Count);
                                            break;
                                        case 5:
                                            int reactType5Count = comment.getReactCount().getReactType5() - 1;
                                            comment.getReactCount().setReactType5(reactType5Count);
                                            break;
                                        case 6:
                                            int reactType6Count = comment.getReactCount().getReactType6() - 1;
                                            comment.getReactCount().setReactType6(reactType6Count);
                                            break;
                                    }
                                    int count = comment.getCounts().getReactCount() - 1;
                                    comment.getCounts().setReactCount(count);
                                }
                                comments.set(position, comment);
                                updateReactComment(comment);
                            }
                        });
                        tvReactActionComment.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v2) {
                                ReactionPopup reactionPopup = ContextExtension.createReactPostPopup(context);
                                reactionPopup.setReactionSelectedListener(new Function1<Integer, Boolean>() {
                                    @Override
                                    public Boolean invoke(Integer integer) {
                                        if (integer >= 0 && integer <= 5) {
                                            int reactType = integer + 1;
                                            int position = getBindingAdapterPosition() - posts.size();
                                            CommentPost comment = comments.get(position);
                                            int oldType = comments.get(position).getReactStatus();
                                            if (oldType != reactType) { // update
                                                onCommentListener.onReactActionLongClick(reactType, position);
                                                if (oldType != 0) {
                                                    switch (oldType) {
                                                        case 1:
                                                            int reactType1Count = comment.getReactCount().getReactType1() - 1;
                                                            comment.getReactCount().setReactType1(reactType1Count);
                                                            break;
                                                        case 2:
                                                            int reactType2Count = comment.getReactCount().getReactType2() - 1;
                                                            comment.getReactCount().setReactType2(reactType2Count);
                                                            break;
                                                        case 3:
                                                            int reactType3Count = comment.getReactCount().getReactType3() - 1;
                                                            comment.getReactCount().setReactType3(reactType3Count);
                                                            break;
                                                        case 4:
                                                            int reactType4Count = comment.getReactCount().getReactType4() - 1;
                                                            comment.getReactCount().setReactType4(reactType4Count);
                                                            break;
                                                        case 5:
                                                            int reactType5Count = comment.getReactCount().getReactType5() - 1;
                                                            comment.getReactCount().setReactType5(reactType5Count);
                                                            break;
                                                        case 6:
                                                            int reactType6Count = comment.getReactCount().getReactType6() - 1;
                                                            comment.getReactCount().setReactType6(reactType6Count);
                                                            break;
                                                    }
                                                } else {
                                                    int count = comment.getCounts().getReactCount() + 1;
                                                    comment.getCounts().setReactCount(count);
                                                }
                                                comment.setReactStatus(reactType);
                                                switch (reactType) {
                                                    case 1:
                                                        int reactType1Count = comment.getReactCount().getReactType1() + 1;
                                                        comment.getReactCount().setReactType1(reactType1Count);
                                                        break;
                                                    case 2:
                                                        int reactType2Count = comment.getReactCount().getReactType2() + 1;
                                                        comment.getReactCount().setReactType2(reactType2Count);
                                                        break;
                                                    case 3:
                                                        int reactType3Count = comment.getReactCount().getReactType3() + 1;
                                                        comment.getReactCount().setReactType3(reactType3Count);
                                                        break;
                                                    case 4:
                                                        int reactType4Count = comment.getReactCount().getReactType4() + 1;
                                                        comment.getReactCount().setReactType4(reactType4Count);
                                                        break;
                                                    case 5:
                                                        int reactType5Count = comment.getReactCount().getReactType5() + 1;
                                                        comment.getReactCount().setReactType5(reactType5Count);
                                                        break;
                                                    case 6:
                                                        int reactType6Count = comment.getReactCount().getReactType6() + 1;
                                                        comment.getReactCount().setReactType6(reactType6Count);
                                                        break;
                                                }
                                                comments.set(position, comment);
                                                updateReactComment(comment);
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
                ivImageComment.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onCommentListener.onItemLongClick(tvUserNameComment, getBindingAdapterPosition() - posts.size());
                        return false;
                    }
                });
                lCommentContainer.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onCommentListener.onItemLongClick(tvUserNameComment, getBindingAdapterPosition() - posts.size());
                        return false;
                    }
                });
            }
        }

        // handler update react comment
        private void updateReactComment(CommentPost comment) {
            // handler react count
            ReactCount reactCount = comment.getReactCount();
            int reactType1 = reactCount.getReactType1();
            int reactType2 = reactCount.getReactType2();
            int reactType3 = reactCount.getReactType3();
            int reactType4 = reactCount.getReactType4();
            int reactType5 = reactCount.getReactType5();
            int reactType6 = reactCount.getReactType6();
            if (comment.getCounts().getReactCount() == 0) {
                lReactComment.setVisibility(View.GONE);
            } else {
                lReactComment.setVisibility(View.VISIBLE);
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
                    ivFirstReactComment.setVisibility(View.VISIBLE);
                    ivFirstReactComment.setImageResource(reactList.get(0).second);
                } else {
                    ivFirstReactComment.setVisibility(View.GONE);
                }
                if (reactList.get(1).first != 0) {
                    ivSecondReactComment.setVisibility(View.VISIBLE);
                    ivSecondReactComment.setImageResource(reactList.get(1).second);
                } else {
                    ivSecondReactComment.setVisibility(View.GONE);
                }
                tvReactCommentCount.setText(StringExtension.formatReactCountPost(comment.getCounts().getReactCount()));
            }
            switch (comment.getReactStatus()) {
                // logged in user don't react with this post
                case 1:
                    tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_like));
                    break;
                case 2:
                    tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_heart));
                    break;
                case 3:
                    tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_haha));
                    break;
                case 4:
                    tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_wow));
                    break;
                case 5:
                    tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_sad));
                    break;
                case 6:
                    tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_angry));
                    break;
                default:
                    tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));
                    break;
            }
        }
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civUserAvatar;
        AppCompatTextView tvUserName, tvGroupName, tvTimePost, tvReactCount, tvCommentCount, tvReactAction;
        AppCompatImageView ivMenu, ivSecondReact, ivFirstReact, ivReactAction;
        ReadMoreTextView tvContent;
        ConstraintLayout lContentToShare, lMediaContent, lReactCount, lEdited;
        AppCompatImageView ivImageViewer, ivVideoThumbnail;
        ProgressBar pbLoadingVideo;
        PlayerView pvVideoViewer;
        ExoPlayer exoPlayer; // controller video
        AppCompatImageButton btnVolume;
        boolean isMute;
        LinearLayoutCompat lReactAction, lShareAction;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // init
            isMute = false;

            // binding
            civUserAvatar = itemView.findViewById(R.id.civ_user_avatar);
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
            lReactCount = itemView.findViewById(R.id.l_react_count);
            tvReactCount = itemView.findViewById(R.id.tv_react_count);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            lMediaContent = itemView.findViewById(R.id.l_media_content);
            ivImageViewer = itemView.findViewById(R.id.iv_image_viewer);
            ivVideoThumbnail = itemView.findViewById(R.id.iv_video_thumbnail);
            pbLoadingVideo = itemView.findViewById(R.id.pb_loading_video);
            pvVideoViewer = itemView.findViewById(R.id.pv_video_viewer);
            btnVolume = pvVideoViewer.findViewById(R.id.btn_volume);
            lReactAction = itemView.findViewById(R.id.l_react_action);
            lShareAction = itemView.findViewById(R.id.l_share_action);
            lContentToShare = itemView.findViewById(R.id.l_content_to_share);

            if (onPostListener != null) {
                civUserAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onUserPostClick();
                    }
                });
                tvUserName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onUserPostClick();
                    }
                });
                tvGroupName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onGroupPostClick();
                    }
                });
                ivMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onMenuItemClick();
                    }
                });
                tvContent.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onPostListener.onContentLongClick();
                        return false;
                    }
                });
                ivImageViewer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onMediaItemClick();
                    }
                });
                lReactCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPostListener.onReactCountClick();
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
                                Post post = posts.get(0);
                                int oldType = posts.get(0).getReactStatus();
                                if (oldType == 0) {
                                    onPostListener.onReactActionClick(1);
                                    post.setReactStatus(1);
                                    int reactType1Count = post.getReactCount().getReactType1() + 1;
                                    post.getReactCount().setReactType1(reactType1Count);
                                    int count = post.getCounts().getReactCount() + 1;
                                    post.getCounts().setReactCount(count);
                                } else {
                                    onPostListener.onReactActionClick(0); // = 0 -> delete
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
                                posts.clear();
                                posts.add(post);
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
                                            int reactType = integer + 1;
                                            Post post = posts.get(0);
                                            int oldType = posts.get(0).getReactStatus();
                                            if (oldType != reactType) { // update
                                                onPostListener.onReactActionLongClick(reactType);
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
                                                posts.clear();
                                                posts.add(post);
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
                        onPostListener.onShareActionClick(lContentToShare);
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
        private void initializePlayer(String media_url) {
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

            currentPlayingVideo = exoPlayer;

            // Set PlayWhenReady. If true, content will autoplay.
            exoPlayer.setPlayWhenReady(true);

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    if (playbackState == Player.STATE_IDLE) {
                        ivVideoThumbnail.setVisibility(View.VISIBLE);
                        pbLoadingVideo.setVisibility(View.VISIBLE);
                        pvVideoViewer.setVisibility(View.GONE);
                    } else if (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY) {
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

                @Override
                public void onPlayerError(PlaybackException error) {
                    releasePlayer();
                    ivVideoThumbnail.setVisibility(View.VISIBLE);
                    pvVideoViewer.setVisibility(View.GONE);
                    Toast.makeText(context, R.string.error_play_video, Toast.LENGTH_SHORT).show();
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

    /**
     * release the player
     */
    public void releasePlayer() {
        if (currentPlayingVideo != null) {
            currentPlayingVideo.release();
            currentPlayingVideo = null;
        }
    }

    // pause video
    public void pausePlayerVideo() {
        if (currentPlayingVideo != null) {
            currentPlayingVideo.setPlayWhenReady(false);
        }
    }

    // continue video
    public void continuePlayerVideo() {
        if (currentPlayingVideo != null) {
            currentPlayingVideo.setPlayWhenReady(true);
        }
    }
}
