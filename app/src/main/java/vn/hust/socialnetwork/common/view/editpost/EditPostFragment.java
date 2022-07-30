package vn.hust.socialnetwork.common.view.editpost;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.post.CommentPost;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.ui.postcreator.PostCreatorActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;

public class EditPostFragment extends DialogFragment {

    private PostService postService;

    private Post post;

    private CircleImageView civMyAvatar;
    private AppCompatTextView tvToolbarConfirm, tvUserName;
    private AppCompatEditText etContentPost;
    private AppCompatImageView ivToolbarBack, ivPreviewMedia, ivPlayVideo;
    private ProgressBar pbLoading;

    private OnEditPostListener onEditPostListener;

    public EditPostFragment() {
        // Required empty public constructor
    }

    public EditPostFragment(Post post, OnEditPostListener onEditPostListener) {
        this.post = post;
        this.onEditPostListener = onEditPostListener;
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
        View view = inflater.inflate(R.layout.fragment_edit_post, container, false);

        // api
        postService = ApiClient.getClient().create(PostService.class);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarConfirm = view.findViewById(R.id.tv_toolbar_confirm);
        civMyAvatar = view.findViewById(R.id.civ_my_avatar);
        tvUserName = view.findViewById(R.id.tv_user_name);
        etContentPost = view.findViewById(R.id.et_content_post);
        ivPreviewMedia = view.findViewById(R.id.iv_preview_media);
        ivPlayVideo = view.findViewById(R.id.iv_play_video);
        pbLoading = view.findViewById(R.id.pb_loading);

        // init data
        Glide.with(EditPostFragment.this)
                .asBitmap()
                .load(post.getUser().getAvatar())
                .error(R.drawable.default_avatar)
                .into(civMyAvatar);
        tvUserName.setText(post.getUser().getName());
        etContentPost.setText(post.getCaption());
        if (post.getMedia() != null) {
            if (post.getMedia().getType().equals(FileExtension.MEDIA_IMAGE_TYPE)) {
                Glide.with(EditPostFragment.this)
                        .asBitmap()
                        .load(post.getMedia().getSrc())
                        .into(ivPreviewMedia);
                ivPlayVideo.setVisibility(View.GONE);
            } else {
                RequestOptions options = new RequestOptions().frame(1000);
                Glide.with(EditPostFragment.this)
                        .load(post.getMedia().getSrc())
                        .apply(options)
                        .into(ivPreviewMedia);
                ivPlayVideo.setVisibility(View.VISIBLE);
            }
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tvToolbarConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePost();
            }
        });

        return view;
    }

    private void updatePost() {
        ContextExtension.hideKeyboard(etContentPost);
        // get value
        String caption = etContentPost.getText().toString().trim();
        if (caption.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_post_creator_empty, Toast.LENGTH_SHORT).show();
        } else if (caption.equals(post.getCaption())) {
            // close dialog
            dismiss();
        } else {
            pbLoading.setVisibility(View.VISIBLE);
            // send api update post
            Map<String, Object> req = new HashMap<>();
            req.put("caption", caption);
            Call<BaseResponse<Post>> call = postService.updatePost(post.getId(), req);
            call.enqueue(new Callback<BaseResponse<Post>>() {
                @Override
                public void onResponse(Call<BaseResponse<Post>> call, Response<BaseResponse<Post>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<Post> res = response.body();
                        // update value
                        post = res.getData();
                        // close fragment
                        onEditPostListener.onConfirmClick(post);
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                    pbLoading.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<BaseResponse<Post>> call, Throwable t) {
                    // error
                    call.cancel();
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    pbLoading.setVisibility(View.GONE);
                }
            });
        }
    }
}