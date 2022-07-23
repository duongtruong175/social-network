package vn.hust.socialnetwork.common.view.commentpost;

import static android.app.Activity.RESULT_OK;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
import vn.hust.socialnetwork.common.view.commentpost.adapters.CommentAdapter;
import vn.hust.socialnetwork.common.view.commentpost.adapters.OnCommentListener;
import vn.hust.socialnetwork.common.view.reactuser.ReactUserFragment;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.fcm.Data;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.notification.Notification;
import vn.hust.socialnetwork.models.post.CommentPost;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.CommentService;
import vn.hust.socialnetwork.network.NotificationService;
import vn.hust.socialnetwork.network.UploadMediaService;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.NotificationExtension;
import vn.hust.socialnetwork.utils.RequestCodeResultActivity;

public class CommentPostFragment extends BottomSheetDialogFragment {

    // id and type: post
    private int postId;
    private Post post;
    private OnBottomSheetDismiss onBottomSheetDismiss;

    private CommentService commentService;
    private UploadMediaService uploadMediaService;
    private NotificationService notificationService;

    private FrameLayout lBottomSheet;
    private SwipeRefreshLayout lSwipeRefresh;
    private ConstraintLayout lError, lEmpty;
    private LinearProgressIndicator pbLoadingComment;
    private ConstraintLayout lInputMediaComment;
    private AppCompatImageView ivUploadPhotoComment, ivCancelUploadPhoto, ivAddEmojiComment, ivAddPhotoComment, ivSendComment;
    private AppCompatEditText etContentComment;
    private EmojiPopup emojiPopup;
    private RecyclerView rvComment;

    private String uploadImageCommentPath;
    private RxPermissions rxPermissions;

    private List<CommentPost> comments;
    private CommentAdapter commentAdapter;

    public CommentPostFragment() {
        // Required empty public constructor
    }

    public CommentPostFragment(Post post, OnBottomSheetDismiss onBottomSheetDismiss) {
        this.post = post;
        if (post != null) {
            postId = post.getId();
        } else {
            postId = 0;
        }
        this.onBottomSheetDismiss = onBottomSheetDismiss;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment_post, container, false);

        // api
        commentService = ApiClient.getClient().create(CommentService.class);
        uploadMediaService = ApiClient.getClient().create(UploadMediaService.class);
        notificationService = ApiClient.getClient().create(NotificationService.class);

        // binding
        lBottomSheet = view.findViewById(R.id.l_bottom_sheet);
        lSwipeRefresh = view.findViewById(R.id.l_swipe_refresh);
        lError = view.findViewById(R.id.l_error);
        lEmpty = view.findViewById(R.id.l_empty);
        pbLoadingComment = view.findViewById(R.id.pb_loading_comment);
        lInputMediaComment = view.findViewById(R.id.l_input_media_comment);
        ivUploadPhotoComment = view.findViewById(R.id.iv_upload_photo_comment);
        ivCancelUploadPhoto = view.findViewById(R.id.iv_cancel_upload_photo);
        ivAddEmojiComment = view.findViewById(R.id.iv_add_emoji_comment);
        ivAddPhotoComment = view.findViewById(R.id.iv_add_photo_comment);
        ivSendComment = view.findViewById(R.id.iv_send_comment);
        etContentComment = view.findViewById(R.id.et_content_comment);
        rvComment = view.findViewById(R.id.rv_comment);

        rxPermissions = new RxPermissions(CommentPostFragment.this);
        // init view
        emojiPopup = EmojiPopup.Builder
                .fromRootView(lBottomSheet)
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
        handleRefreshView();
        uploadImageCommentPath = "";
        ivSendComment.setEnabled(false);

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
                                MediaPicker.openImagePicker(CommentPostFragment.this, RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE, 1, false);
                            } else {
                                Toast.makeText(getContext(), R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
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

        lError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        // setup recycle view
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), comments, new OnCommentListener() {
            @Override
            public void onUserCommentClick(int position) {
                // open user detail
                Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                intent.putExtra("user_id", comments.get(position).getUser().getId());
                startActivity(intent);
            }

            @Override
            public void onMediaItemClick(int position) {
                Media media = comments.get(position).getMedia();
                if (media != null && checkValidValueString(media.getSrc()) && media.getType().equals(FileExtension.MEDIA_IMAGE_TYPE)) {
                    Intent viewPhotoIntent = new Intent(getActivity(), MediaViewerActivity.class);
                    viewPhotoIntent.putExtra("media_url", media.getSrc());
                    viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                    startActivity(viewPhotoIntent);
                }
            }

            @Override
            public void onReactCountClick(int position) {
                BottomSheetDialogFragment bottomSheetDialogFragment = ReactUserFragment.newInstance(comments.get(position).getId(), "comment");
                bottomSheetDialogFragment.show(getParentFragmentManager(), bottomSheetDialogFragment.getTag());
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvComment.setLayoutManager(layoutManager);
        rvComment.setHasFixedSize(true);
        rvComment.setAdapter(commentAdapter);
        rvComment.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!lSwipeRefresh.isRefreshing()) {
                    lSwipeRefresh.setEnabled(!rvComment.canScrollVertically(-1));
                }
            }
        });

        // get data
        getData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View bottomSheet = (View) view.getParent();
        bottomSheet.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
        bottomSheet.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        bottomSheet.setBackgroundColor(Color.TRANSPARENT);
        // line to fix bottom sheet moved up when open keyboard
        new KeyboardUtil(requireActivity(), view);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // use interface to callback method
        if (lError != null && lError.getVisibility() == View.GONE) {
            onBottomSheetDismiss.onDialogDismiss(comments.size());
        }
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        // When using AndroidX the resource can be found at com.google.android.material.R.id.design_bottom_sheet
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = WindowManager.LayoutParams.MATCH_PARENT;
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        // if want dialog display fullscreen when opening, uncomment the line below
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE && resultCode == RESULT_OK && data != null) {
            ivAddPhotoComment.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_text_highlight));
            uploadImageCommentPath = Matisse.obtainPathResult(data).get(0);
            lInputMediaComment.setVisibility(View.VISIBLE);
            Glide.with(CommentPostFragment.this)
                    .asBitmap()
                    .load(uploadImageCommentPath)
                    .into(ivUploadPhotoComment);
        }
    }

    private void getData() {
        lError.setVisibility(View.GONE);
        pbLoadingComment.setVisibility(View.VISIBLE);
        // call api
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
        getCommentPost(postId);
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
                    commentAdapter.notifyDataSetChanged();
                    lError.setVisibility(View.GONE);
                    if (comments.size() == 0) {
                        lEmpty.setVisibility(View.VISIBLE);
                    } else {
                        lEmpty.setVisibility(View.GONE);
                    }
                } else {
                    lError.setVisibility(View.VISIBLE);
                }
                pbLoadingComment.setVisibility(View.GONE);
                lSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CommentPost>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoadingComment.setVisibility(View.GONE);
                lError.setVisibility(View.VISIBLE);
                lSwipeRefresh.setRefreshing(false);
            }
        });
    }

    private void uploadComment() {
        if (emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        }
        ContextExtension.hideKeyboard(etContentComment);
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
                        Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<Media>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    pbLoadingComment.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
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
                    commentAdapter.notifyItemInserted(comments.size() - 1);
                    rvComment.smoothScrollToPosition(comments.size() - 1);
                    lEmpty.setVisibility(View.GONE);

                    // send a notification
                    if (Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0) != post.getUser().getId()) {
                        int receiverId = post.getUser().getId();
                        String imageUrl = "";
                        if (!Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "").isEmpty()) {
                            imageUrl = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "");
                        }
                        int type = 3;
                        String contentNotification = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, "") + " đã bình luận về bài viết của bạn: " + content;
                        String url = "post/" + postId;
                        sendNotification(receiverId, imageUrl, type, contentNotification, url);
                    }
                } else {
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoadingComment.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<CommentPost>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                pbLoadingComment.setVisibility(View.GONE);
                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
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
                    commentAdapter.notifyItemRemoved(position);
                } else {
                    Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
                pbLoadingComment.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                // error
                call.cancel();
                Toast.makeText(getContext(), R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                pbLoadingComment.setVisibility(View.GONE);
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
        ivAddPhotoComment.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_drawable_post));
        uploadImageCommentPath = "";
        Glide.with(CommentPostFragment.this)
                .clear(ivUploadPhotoComment);
        lInputMediaComment.setVisibility(View.GONE);
    }

    private void showPopupMenuComment(View view, int position) {
        CommentPost comment = comments.get(position);
        Context wrapper = new ContextThemeWrapper(getContext(), R.style.CustomPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, view, Gravity.CENTER_VERTICAL);
        // check if the comment is from the user logged in
        popupMenu.getMenuInflater().inflate(R.menu.menu_comment_post, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_copy_content) {
                    ContextExtension.copyToClipboard(requireContext(), comment.getContent());
                    Toast.makeText(getContext(), R.string.content_copied, Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.action_edit_comment) {
                    // open fragment edit comment

                } else if (item.getItemId() == R.id.action_delete_comment) {
                    // open dialog confirm delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme);
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
                    Toast.makeText(getContext(), R.string.report_comment_success, Toast.LENGTH_SHORT).show();
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

    private void sendNotification(int receiverId, String imageUrl, int type, String content, String url) {
        Map<String, Object> req = new HashMap<>();
        req.put("receiver_id", receiverId);
        req.put("type", type);
        req.put("content", content);
        req.put("url", url);
        if (!imageUrl.isEmpty()) {
            req.put("image_url", imageUrl);
        }
        Call<BaseResponse<Notification>> call = notificationService.createNotification(req);
        call.enqueue(new Callback<BaseResponse<Notification>>() {
            @Override
            public void onResponse(Call<BaseResponse<Notification>> call, Response<BaseResponse<Notification>> response) {
                if (response.isSuccessful()) {
                    // send a notification to FCM
                    Data data = new Data("Social Network", content, "notification", url);
                    NotificationExtension.sendFCMNotification(receiverId, data);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Notification>> call, Throwable t) {
                // error
                call.cancel();
            }
        });
    }
}