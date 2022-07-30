package vn.hust.socialnetwork.common.view.editcomment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.post.CommentPost;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.CommentService;
import vn.hust.socialnetwork.utils.ContextExtension;

public class EditCommentFragment extends DialogFragment {

    private CommentService commentService;

    private CommentPost comment;

    private CircleImageView civImageAvatarComment;
    private AppCompatEditText etContentComment;
    private AppCompatImageView ivToolbarBack, ivAddEmojiComment, ivImageComment;
    private EmojiPopup emojiPopup;
    private AppCompatTextView tvToolbarConfirm;
    private ProgressBar pbLoading;

    private OnEditCommentListener onEditCommentListener;

    public EditCommentFragment() {
        // Required empty public constructor
    }

    public EditCommentFragment(CommentPost comment, OnEditCommentListener onEditCommentListener) {
        this.comment = comment;
        this.onEditCommentListener = onEditCommentListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_comment, container, false);

        // api
        commentService = ApiClient.getClient().create(CommentService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        civImageAvatarComment = view.findViewById(R.id.civ_image_avatar_comment);
        etContentComment = view.findViewById(R.id.et_content_comment);
        ivAddEmojiComment = view.findViewById(R.id.iv_add_emoji_comment);
        ivImageComment = view.findViewById(R.id.iv_image_comment);
        pbLoading = view.findViewById(R.id.pb_loading);

        // init data
        emojiPopup = EmojiPopup.Builder
                .fromRootView(view)
                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        ivAddEmojiComment.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_drawable_post));
                    }
                })
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        ivAddEmojiComment.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_text_highlight));
                    }
                })
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .build(etContentComment);
        Glide.with(EditCommentFragment.this)
                .asBitmap()
                .load(comment.getUser().getAvatar())
                .error(R.drawable.default_avatar)
                .into(civImageAvatarComment);
        etContentComment.setText(comment.getContent());
        if (comment.getMedia() != null) {
            Glide.with(EditCommentFragment.this)
                    .asBitmap()
                    .load(comment.getMedia().getSrc())
                    .into(ivImageComment);
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // handle edittext comment
        etContentComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                }
                ContextExtension.showKeyboard(etContentComment);
            }
        });

        ivAddEmojiComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiPopup.toggle();
            }
        });

        tvToolbarConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateComment();
            }
        });

        return view;
    }


    private void updateComment() {
        if (emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        }
        ContextExtension.hideKeyboard(etContentComment);
        // get value
        String content = etContentComment.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_comment_empty, Toast.LENGTH_SHORT).show();
        } else if (content.equals(comment.getContent())) {
            // close dialog
            dismiss();
        } else {
            pbLoading.setVisibility(View.VISIBLE);
            // send api update comment
            Map<String, Object> req = new HashMap<>();
            req.put("content", content);
            Call<BaseResponse<CommentPost>> call = commentService.updateComment(comment.getId(), req);
            call.enqueue(new Callback<BaseResponse<CommentPost>>() {
                @Override
                public void onResponse(Call<BaseResponse<CommentPost>> call, Response<BaseResponse<CommentPost>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<CommentPost> res = response.body();
                        // update value
                        comment = res.getData();
                        // close fragment
                        onEditCommentListener.onConfirmClick(comment);
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                    pbLoading.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<BaseResponse<CommentPost>> call, Throwable t) {
                    // error
                    call.cancel();
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    pbLoading.setVisibility(View.GONE);
                }
            });
        }
    }
}