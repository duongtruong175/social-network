package vn.hust.socialnetwork.common.view.commentpost.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.pgreze.reactions.ReactionPopup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.jvm.functions.Function1;
import kr.co.prnd.readmore.ReadMoreTextView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.CommentPost;
import vn.hust.socialnetwork.models.post.ReactCount;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.StringExtension;
import vn.hust.socialnetwork.utils.TimeExtension;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>{

    private Context context;
    private List<CommentPost> comments;
    private OnCommentListener onCommentListener;

    public CommentAdapter(Context context, List<CommentPost> comments, OnCommentListener onCommentListener) {
        this.context = context;
        this.comments = comments;
        this.onCommentListener = onCommentListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentPost comment = comments.get(position);
        Glide.with(context)
                .asBitmap()
                .load(comment.getUser().getAvatar())
                .error(R.drawable.default_avatar)
                .into(holder.civImageAvatarComment);
        holder.tvUserNameComment.setText(comment.getUser().getName());
        holder.tvContentComment.setText(StringExtension.cleanContent(comment.getContent()));
        holder.tvTimeComment.setText(TimeExtension.formatTimeComment(comment.getUpdatedAt()));
        if (comment.getCreatedAt().equals(comment.getUpdatedAt())) {
            holder.lEdited.setVisibility(View.GONE);
        } else {
            holder.lEdited.setVisibility(View.VISIBLE);
        }
        Media media = comment.getMedia();
        if (media != null) {
            holder.lCommentMedia.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .asBitmap()
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .load(media.getSrc())
                    .into(holder.ivImageComment);
        } else {
            holder.lCommentMedia.setVisibility(View.GONE);
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
            holder.lReactComment.setVisibility(View.GONE);
        } else {
            holder.lReactComment.setVisibility(View.VISIBLE);
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
                holder.ivFirstReactComment.setVisibility(View.VISIBLE);
                holder.ivFirstReactComment.setImageResource(reactList.get(0).second);
            } else {
                holder.ivFirstReactComment.setVisibility(View.GONE);
            }
            if (reactList.get(1).first != 0) {
                holder.ivSecondReactComment.setVisibility(View.VISIBLE);
                holder.ivSecondReactComment.setImageResource(reactList.get(1).second);
            } else {
                holder.ivSecondReactComment.setVisibility(View.GONE);
            }
            holder.tvReactCommentCount.setText(StringExtension.formatReactCountPost(comment.getCounts().getReactCount()));
        }
        switch (comment.getReactStatus()) {
            // logged in user don't react with this post
            case 1:
                holder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_like));
                break;
            case 2:
                holder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_heart));
                break;
            case 3:
                holder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_haha));
                break;
            case 4:
                holder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_wow));
                break;
            case 5:
                holder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_sad));
                break;
            case 6:
                holder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_angry));
                break;
            default:
                holder.tvReactActionComment.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
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
                        onCommentListener.onUserCommentClick(getBindingAdapterPosition());
                    }
                });
                tvUserNameComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onUserCommentClick(getBindingAdapterPosition());
                    }
                });
                tvContentComment.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onCommentListener.onItemLongClick(tvUserNameComment, getBindingAdapterPosition());
                        return false;
                    }
                });
                ivImageComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onMediaItemClick(getBindingAdapterPosition());
                    }
                });
                lReactComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onReactCountClick(getBindingAdapterPosition());
                    }
                });
                tvReplyComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommentListener.onReplyActionClick(getBindingAdapterPosition());
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
                                int position = getBindingAdapterPosition();
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
                                            int position = getBindingAdapterPosition();
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
                        onCommentListener.onItemLongClick(tvUserNameComment, getBindingAdapterPosition());
                        return false;
                    }
                });
                lCommentContainer.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onCommentListener.onItemLongClick(tvUserNameComment, getBindingAdapterPosition());
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
}
