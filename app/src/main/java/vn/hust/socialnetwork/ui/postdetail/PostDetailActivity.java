package vn.hust.socialnetwork.ui.postdetail;

import static vn.hust.socialnetwork.utils.ContextExtension.getImageFromLayout;
import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.orhanobut.hawk.Hawk;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.view.reactuser.ReactUserFragment;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.CommentPost;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.CommentService;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.network.UploadMediaService;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.ui.postcreator.PostCreatorActivity;
import vn.hust.socialnetwork.ui.postdetail.adapters.OnPostListener;
import vn.hust.socialnetwork.ui.postdetail.adapters.PostDetailAdapter;
import vn.hust.socialnetwork.ui.postdetail.adapters.OnCommentListener;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.RequestCodeResultActivity;

public class PostDetailActivity extends AppCompatActivity {

    private SwipeRefreshLayout lSwipeRefresh;
    private ProgressBar pbLoading;
    private ConstraintLayout lMain, lError;
    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle;

    private RecyclerView rvPostDetail;

    private LinearProgressIndicator pbLoadingComment;
    private ConstraintLayout lInputMediaComment;
    private AppCompatImageView ivUploadPhotoComment, ivCancelUploadPhoto, ivAddEmojiComment, ivAddPhotoComment, ivSendComment;
    private AppCompatEditText etContentComment;
    private EmojiPopup emojiPopup;

    private PostService postService;
    private CommentService commentService;
    private UploadMediaService uploadMediaService;
    private int postId;
    private boolean isShowCommentInput;
    private String uploadImageCommentPath;
    private RxPermissions rxPermissions;

    private Post post;
    private List<Post> viewPosts; // only a item
    private List<CommentPost> comments;
    private PostDetailAdapter postDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            postId = extras.getInt("post_id");
            isShowCommentInput = extras.getBoolean("is_show_comment_input");
        } else {
            postId = 0;
            isShowCommentInput = false;
        }
        rxPermissions = new RxPermissions(PostDetailActivity.this);

        // api
        postService = ApiClient.getClient().create(PostService.class);
        commentService = ApiClient.getClient().create(CommentService.class);
        uploadMediaService = ApiClient.getClient().create(UploadMediaService.class);

        // binding
        lSwipeRefresh = findViewById(R.id.l_swipe_refresh);
        pbLoading = findViewById(R.id.pb_loading);
        lMain = findViewById(R.id.l_main);
        lError = findViewById(R.id.l_error);
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        rvPostDetail = findViewById(R.id.rv_post_detail);
        pbLoadingComment = findViewById(R.id.pb_loading_comment);
        lInputMediaComment = findViewById(R.id.l_input_media_comment);
        ivUploadPhotoComment = findViewById(R.id.iv_upload_photo_comment);
        ivCancelUploadPhoto = findViewById(R.id.iv_cancel_upload_photo);
        ivAddEmojiComment = findViewById(R.id.iv_add_emoji_comment);
        ivAddPhotoComment = findViewById(R.id.iv_add_photo_comment);
        ivSendComment = findViewById(R.id.iv_send_comment);
        etContentComment = findViewById(R.id.et_content_comment);

        // init view
        emojiPopup = EmojiPopup.Builder
                .fromRootView(lSwipeRefresh)
                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        ivAddEmojiComment.setColorFilter(ContextCompat.getColor(PostDetailActivity.this, R.color.color_drawable_post));
                    }
                })
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        ivAddEmojiComment.setColorFilter(ContextCompat.getColor(PostDetailActivity.this, R.color.color_text_highlight));
                    }
                })
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .build(etContentComment);
        handleRefreshView();
        uploadImageCommentPath = "";
        ivSendComment.setEnabled(false);

        tvToolbarTitle.setText(R.string.toolbar_title_post_detail);
        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

        etContentComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etContentComment.getText().toString().trim().length() > 0) {
                    ivSendComment.setEnabled(true);
                } else {
                    ivSendComment.setEnabled(false);
                }
            }
        });

        ivAddPhotoComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) {
                                MediaPicker.openImagePicker(PostDetailActivity.this, RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE, 1, false);
                            } else {
                                Toast.makeText(PostDetailActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        ivCancelUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearUploadPhotoComment();
            }
        });

        ivAddEmojiComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiPopup.toggle();
            }
        });

        // create a comment
        ivSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadComment();
            }
        });

        setupRecycleView();

        // get data
        getData();

        if (isShowCommentInput) {
            ContextExtension.showKeyboard(etContentComment);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        postDetailAdapter.pausePlayerVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        postDetailAdapter.continuePlayerVideo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postDetailAdapter.releasePlayer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE && resultCode == RESULT_OK && data != null) {
            ivAddPhotoComment.setColorFilter(ContextCompat.getColor(PostDetailActivity.this, R.color.color_text_highlight));
            uploadImageCommentPath = Matisse.obtainPathResult(data).get(0);
            lInputMediaComment.setVisibility(View.VISIBLE);
            Glide.with(PostDetailActivity.this)
                    .asBitmap()
                    .load(uploadImageCommentPath)
                    .into(ivUploadPhotoComment);
        }
    }

    private void getData() {
        lMain.setVisibility(View.GONE);
        lError.setVisibility(View.GONE);
        pbLoading.setVisibility(View.VISIBLE);
        // call api
        getPostDetail(postId);
        getCommentPost(postId);
    }

    private void handleRefreshView() {
        lSwipeRefresh.setColorSchemeResources(R.color.colorAccent);
        lSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void refresh() {
        getPostDetail(postId);
        getCommentPost(postId);
    }

    private void setupRecycleView() {
        viewPosts = new ArrayList<>();
        comments = new ArrayList<>();
        postDetailAdapter = new PostDetailAdapter(PostDetailActivity.this, viewPosts, new OnPostListener() {
            @Override
            public void onUserPostClick() {
                // open user detail
                Intent intent = new Intent(PostDetailActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", post.getUser().getId());
                startActivity(intent);
            }

            @Override
            public void onGroupPostClick() {
                // open group detail
            }

            @Override
            public void onMenuItemClick() {
                // open menu of post
                showMenuPost();
            }

            @Override
            public void onContentLongClick() {
                Dialog dialog = new Dialog(PostDetailActivity.this);
                WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.gravity = Gravity.CENTER | Gravity.START;
                layoutParams.x = 100;
                dialog.setContentView(R.layout.dialog_copy_content_post);
                dialog.setCancelable(true);
                dialog.findViewById(R.id.tv_copy_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ContextExtension.copyToClipboard(PostDetailActivity.this, post.getCaption());
                        Toast.makeText(PostDetailActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }

            @Override
            public void onMediaItemClick() {
                Media media = post.getMedia();
                if (media != null && checkValidValueString(media.getSrc()) && media.getType().equals(FileExtension.MEDIA_IMAGE_TYPE)) {
                    Intent viewPhotoIntent = new Intent(PostDetailActivity.this, MediaViewerActivity.class);
                    viewPhotoIntent.putExtra("media_url", media.getSrc());
                    viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                    startActivity(viewPhotoIntent);
                }
            }

            @Override
            public void onReactCountClick() {
                BottomSheetDialogFragment bottomSheetDialogFragment = ReactUserFragment.newInstance(post.getId(), "post");
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }

            @Override
            public void onReactActionClick(int reactType) {
                createOrUpdateReactPost(reactType);
            }

            @Override
            public void onReactActionLongClick(int reactType) {
                createOrUpdateReactPost(reactType);
            }

            @Override
            public void onShareActionClick(View lContentToShare) {
                // open bottom sheet share post
                showSharePost(lContentToShare);
            }
        }, comments, new OnCommentListener() {
            @Override
            public void onUserCommentClick(int position) {
                // open user detail
                Intent intent = new Intent(PostDetailActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", comments.get(position).getUser().getId());
                startActivity(intent);
            }

            @Override
            public void onMediaItemClick(int position) {
                Media media = comments.get(position).getMedia();
                if (media != null && checkValidValueString(media.getSrc()) && media.getType().equals(FileExtension.MEDIA_IMAGE_TYPE)) {
                    Intent viewPhotoIntent = new Intent(PostDetailActivity.this, MediaViewerActivity.class);
                    viewPhotoIntent.putExtra("media_url", media.getSrc());
                    viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                    startActivity(viewPhotoIntent);
                }
            }

            @Override
            public void onReactCountClick(int position) {
                BottomSheetDialogFragment bottomSheetDialogFragment = ReactUserFragment.newInstance(comments.get(position).getId(), "comment");
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
            }

            @Override
            public void onReactActionClick(int reactType, int position) {
                createOrUpdateReactComment(reactType, position);
            }

            @Override
            public void onReactActionLongClick(int reactType, int position) {
                createOrUpdateReactComment(reactType, position);
            }

            @Override
            public void onReplyActionClick(int position) {
                // update later
            }

            @Override
            public void onItemLongClick(View view, int position) {
                showPopupMenuComment(view, position);
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PostDetailActivity.this);
        rvPostDetail.setLayoutManager(layoutManager);
        rvPostDetail.setHasFixedSize(true);
        rvPostDetail.setAdapter(postDetailAdapter);
    }

    private void getPostDetail(int postId) {
        Call<BaseResponse<Post>> call = postService.getPostDetail(postId);
        call.enqueue(new Callback<BaseResponse<Post>>() {
            @Override
            public void onResponse(Call<BaseResponse<Post>> call, Response<BaseResponse<Post>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Post> res = response.body();
                    post = res.getData();
                    viewPosts.clear();
                    viewPosts.add(post);
                    postDetailAdapter.notifyItemChanged(0);
                    lMain.setVisibility(View.VISIBLE);
                    lError.setVisibility(View.GONE);
                } else {
                    lMain.setVisibility(View.GONE);
                    lError.setVisibility(View.VISIBLE);
                }
                pbLoading.setVisibility(View.GONE);
                lSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<Post>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                lMain.setVisibility(View.GONE);
                pbLoading.setVisibility(View.GONE);
                lError.setVisibility(View.VISIBLE);
                lSwipeRefresh.setRefreshing(false);
            }
        });
    }

    private void getCommentPost(int postId) {
        Call<BaseResponse<List<CommentPost>>> call = commentService.getCommentsByPostId(postId);
        call.enqueue(new Callback<BaseResponse<List<CommentPost>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<CommentPost>>> call, Response<BaseResponse<List<CommentPost>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<CommentPost>> res = response.body();
                    comments.clear();
                    comments.addAll(res.getData());
                    postDetailAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CommentPost>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
            }
        });
    }

    private void uploadComment() {
        if (emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        }
        ContextExtension.hideKeyboard(PostDetailActivity.this);
        pbLoadingComment.setVisibility(View.VISIBLE);
        // get data before reset input
        String uploadImagePath = uploadImageCommentPath;
        String content = etContentComment.getText().toString().trim();
        // reset input
        etContentComment.setText("");
        clearUploadPhotoComment();

        // if has media
        if (checkValidValueString(uploadImagePath)) {
            File file = new File(uploadImagePath);
            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part mediaUpload = MultipartBody.Part.createFormData("media", file.getName(), requestFile);
            RequestBody type = RequestBody.create(FileExtension.MEDIA_IMAGE_TYPE, okhttp3.MultipartBody.FORM);
            Call<BaseResponse<Media>> call = uploadMediaService.uploadMedia(mediaUpload, type);
            call.enqueue(new Callback<BaseResponse<Media>>() {
                @Override
                public void onResponse(Call<BaseResponse<Media>> call, Response<BaseResponse<Media>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<Media> res = response.body();
                        Media media = res.getData();
                        uploadCommentWithMedia(content, media);
                    } else {
                        pbLoadingComment.setVisibility(View.GONE);
                        Toast.makeText(PostDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<Media>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    pbLoadingComment.setVisibility(View.GONE);
                    Toast.makeText(PostDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            uploadCommentWithMedia(content, null);
        }
    }

    private void uploadCommentWithMedia(String content, Media media) {
        Map<String, Object> req = new HashMap<>();
        req.put("content", content);
        req.put("post_id", postId);
        if (media != null) {
            req.put("media_id", media.getId());
        }

        Call<BaseResponse<CommentPost>> call = commentService.createComment(req);
        call.enqueue(new Callback<BaseResponse<CommentPost>>() {
            @Override
            public void onResponse(Call<BaseResponse<CommentPost>> call, Response<BaseResponse<CommentPost>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<CommentPost> res = response.body();
                    comments.add(res.getData());
                    postDetailAdapter.notifyItemInserted(comments.size());
                    rvPostDetail.smoothScrollToPosition(comments.size());
                } else {
                    Toast.makeText(PostDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoadingComment.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<CommentPost>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoadingComment.setVisibility(View.GONE);
                Toast.makeText(PostDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteComment(int position) {
        pbLoadingComment.setVisibility(View.VISIBLE);
        int commentId = comments.get(position).getId();
        Call<BaseResponse<String>> call = commentService.deleteComment(commentId);
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    comments.remove(position);
                    postDetailAdapter.notifyItemRemoved(position + 1);
                } else {
                    Toast.makeText(PostDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoadingComment.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(PostDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                pbLoadingComment.setVisibility(View.GONE);
            }
        });
    }

    private void deletePost() {
        Call<BaseResponse<String>> call = postService.deletePost(postId);
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    finish();
                } else {
                    Toast.makeText(PostDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(PostDetailActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrUpdateReactPost(int reactType) {
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        Call<BaseResponse<ReactUser>> call = postService.reactPost(postId, userId, reactType);
        call.enqueue(new Callback<BaseResponse<ReactUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<ReactUser>> call, Response<BaseResponse<ReactUser>> response) {
                // no action
            }

            @Override
            public void onFailure(Call<BaseResponse<ReactUser>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void createOrUpdateReactComment(int reactType, int position) {
        int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
        int commentId = comments.get(position).getId();
        Call<BaseResponse<ReactUser>> call = commentService.reactComment(commentId, userId, reactType);
        call.enqueue(new Callback<BaseResponse<ReactUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<ReactUser>> call, Response<BaseResponse<ReactUser>> response) {
                // no action
            }

            @Override
            public void onFailure(Call<BaseResponse<ReactUser>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void clearUploadPhotoComment() {
        ivAddPhotoComment.setColorFilter(ContextCompat.getColor(PostDetailActivity.this, R.color.color_drawable_post));
        uploadImageCommentPath = "";
        Glide.with(PostDetailActivity.this)
                .clear(ivUploadPhotoComment);
        lInputMediaComment.setVisibility(View.GONE);
    }

    private void showSharePost(View lContentToShare) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PostDetailActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_share_post);

        ConstraintLayout lShareWritePost = bottomSheetDialog.findViewById(R.id.l_share_write_post);
        ConstraintLayout lShareCopyLinkPost = bottomSheetDialog.findViewById(R.id.l_share_copy_link_post);
        ConstraintLayout lShareWithFacebookPost = bottomSheetDialog.findViewById(R.id.l_share_with_facebook_post);
        ConstraintLayout lShareWithMessengerPost = bottomSheetDialog.findViewById(R.id.l_share_with_messenger_post);
        ConstraintLayout lShareOtherPost = bottomSheetDialog.findViewById(R.id.l_share_other_post);

        if (lShareWritePost != null && lShareCopyLinkPost != null && lShareWithFacebookPost != null && lShareWithMessengerPost != null && lShareOtherPost != null) {
            lShareWritePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    // open write post
                    File file = getShareViewFilePath(lContentToShare);
                    if (file != null) {
                        Intent shareIntent = new Intent(PostDetailActivity.this, PostCreatorActivity.class);
                        shareIntent.putExtra("photo_share", file.getAbsoluteFile().toString());
                        startActivity(shareIntent);
                    }
                }
            });
            lShareCopyLinkPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/post/" + postId;
                    ContextExtension.copyToClipboard(PostDetailActivity.this, link);
                    Toast.makeText(PostDetailActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lShareWithFacebookPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.katana"; // Facebook App package
                    Intent intent = PostDetailActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
                    if (intent != null) {
                        File file = getShareViewFilePath(lContentToShare);
                        if (file != null) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            shareIntent.setPackage(applicationPackage);
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_facebook)));
                        }
                    } else {
                        Toast.makeText(PostDetailActivity.this, R.string.no_facebook_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareWithMessengerPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    String applicationPackage = "com.facebook.orca"; // Message App package
                    Intent intent = PostDetailActivity.this.getPackageManager().getLaunchIntentForPackage(applicationPackage);
                    if (intent != null) {
                        File file = getShareViewFilePath(lContentToShare);
                        if (file != null) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            shareIntent.setPackage(applicationPackage);
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_messenger)));
                        }
                    } else {
                        Toast.makeText(PostDetailActivity.this, R.string.no_messager_install, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            lShareOtherPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    File file = getShareViewFilePath(lContentToShare);
                    if (file != null) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_application)));
                    }
                }
            });
        }

        bottomSheetDialog.show();
    }

    private File getShareViewFilePath(View lContentToShare) {
        ConstraintLayout lContent = (ConstraintLayout) lContentToShare;
        if (lContent != null) {
            try {
                if (post.getMedia().getType().equals(FileExtension.MEDIA_VIDEO_TYPE)) {
                    lContent.findViewById(R.id.iv_video_thumbnail).setVisibility(View.VISIBLE);
                }
                File file = getImageFromLayout(lContent);
                if (post.getMedia().getType().equals(FileExtension.MEDIA_VIDEO_TYPE)) {
                    lContent.findViewById(R.id.iv_video_thumbnail).setVisibility(View.GONE);
                }
                return file;
            } catch (IOException e) {
                Toast.makeText(PostDetailActivity.this, R.string.error_generate_image_from_layout, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private void showMenuPost() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PostDetailActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_menu_post);

        ConstraintLayout lCopyContentPost = bottomSheetDialog.findViewById(R.id.l_copy_content_post);
        ConstraintLayout lCopyLinkPost = bottomSheetDialog.findViewById(R.id.l_copy_link_post);
        ConstraintLayout lEditPost = bottomSheetDialog.findViewById(R.id.l_edit_post);
        ConstraintLayout lDeletePost = bottomSheetDialog.findViewById(R.id.l_delete_post);
        ConstraintLayout lReportPost = bottomSheetDialog.findViewById(R.id.l_repost_post);

        if (lCopyContentPost != null && lCopyLinkPost != null && lEditPost != null && lDeletePost != null && lReportPost != null) {
            if (post.getUser().getId() != Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0)) {
                lEditPost.setVisibility(View.GONE);
                lDeletePost.setVisibility(View.GONE);
            } else {
                lReportPost.setVisibility(View.GONE);
            }

            lCopyContentPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    ContextExtension.copyToClipboard(PostDetailActivity.this, post.getCaption());
                    Toast.makeText(PostDetailActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lCopyLinkPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    String link = getString(R.string.share_link_base) + "/post/" + postId;
                    ContextExtension.copyToClipboard(PostDetailActivity.this, link);
                    Toast.makeText(PostDetailActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();
                }
            });
            lEditPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    // open fragment edit post

                }
            });
            lDeletePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    // open dialog confirm delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this, R.style.AlertDialogTheme);
                    builder.setTitle(R.string.menu_delete_post);
                    builder.setMessage(R.string.do_you_realy_want_to_delete_this_post);
                    builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            deletePost();
                        }
                    });
                    builder.setNegativeButton(R.string.not_agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
            lReportPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                    // open activity report post
                    Toast.makeText(PostDetailActivity.this, R.string.report_post_success, Toast.LENGTH_SHORT).show();
                }
            });
        }

        bottomSheetDialog.show();
    }

    private void showPopupMenuComment(View view, int position) {
        CommentPost comment = comments.get(position);
        Context wrapper = new ContextThemeWrapper(PostDetailActivity.this, R.style.CustomPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, view, Gravity.CENTER_VERTICAL);
        // check if the comment is from the user logged in
        popupMenu.getMenuInflater().inflate(R.menu.menu_comment_post, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_copy_content) {
                    ContextExtension.copyToClipboard(PostDetailActivity.this, comment.getContent());
                    Toast.makeText(PostDetailActivity.this, R.string.content_copied, Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.action_edit_comment) {
                    // open fragment edit comment

                } else if (item.getItemId() == R.id.action_delete_comment) {
                    // open dialog confirm delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this, R.style.AlertDialogTheme);
                    builder.setTitle(R.string.delete_comment);
                    builder.setMessage(R.string.do_you_realy_want_to_delete_this_comment);
                    builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            deleteComment(position);
                        }
                    });
                    builder.setNegativeButton(R.string.not_agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                } else if (item.getItemId() == R.id.action_report_comment) {
                    // open activity report comment
                    Toast.makeText(PostDetailActivity.this, R.string.report_comment_success, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        if (comment.getUser().getId() != Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0)) {
            popupMenu.getMenu().removeItem(R.id.action_edit_comment);
        } else {
            popupMenu.getMenu().removeItem(R.id.action_report_comment);
        }
        if (post.getUser().getId() != Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0)) {
            popupMenu.getMenu().removeItem(R.id.action_delete_comment);
        }
        popupMenu.setForceShowIcon(true);
        // Showing the popup menu
        popupMenu.show();
    }
}