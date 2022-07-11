package vn.hust.socialnetwork.ui.postcreator;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.orhanobut.hawk.Hawk;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.network.UploadMediaService;
import vn.hust.socialnetwork.ui.postdetail.PostDetailActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.NotificationExtension;
import vn.hust.socialnetwork.utils.RequestCodeResultActivity;

public class PostCreatorActivity extends AppCompatActivity {

    private PostService postService;
    private UploadMediaService uploadMediaService;

    private RxPermissions rxPermissions;

    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm, tvUserName;
    private CircleImageView civMyAvatar;
    private AppCompatImageView ivToolbarBack, ivAddPhoto, ivAddVideo, ivPreviewMedia, ivClearPickedMedia, ivPlayVideo;
    private AppCompatEditText etContentPost;
    private Dialog progressDialog;

    private String fileUploadPath, fileUploadType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creator);

        // api
        postService = ApiClient.getClient().create(PostService.class);
        uploadMediaService = ApiClient.getClient().create(UploadMediaService.class);

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        civMyAvatar = findViewById(R.id.civ_my_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvToolbarConfirm = findViewById(R.id.tv_toolbar_confirm);
        etContentPost = findViewById(R.id.et_content_post);
        ivAddPhoto = findViewById(R.id.iv_add_photo);
        ivAddVideo = findViewById(R.id.iv_add_video);
        ivPlayVideo = findViewById(R.id.iv_play_video);
        ivPreviewMedia = findViewById(R.id.iv_preview_media);
        ivClearPickedMedia = findViewById(R.id.iv_clear_picked_media);

        rxPermissions = new RxPermissions(PostCreatorActivity.this);

        progressDialog = ContextExtension.createProgressDialog(PostCreatorActivity.this);
        tvToolbarTitle.setText(R.string.toolbar_title_post_create);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_post_confirm);

        // init
        String avatar = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_AVATAR_KEY, "");
        Glide.with(PostCreatorActivity.this)
                .asBitmap()
                .load(avatar)
                .error(R.drawable.default_avatar)
                .into(civMyAvatar);
        tvUserName.setText(Hawk.get(AppSharedPreferences.LOGGED_IN_USER_NAME_KEY, ""));

        // init view
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("open_photo")) {
                openPhotoPicker();
            }
            fileUploadPath = extras.getString("photo_share");
            if (checkValidValueString(fileUploadPath)) {
                fileUploadType = "image";
                Glide.with(PostCreatorActivity.this)
                        .asBitmap()
                        .load(fileUploadPath)
                        .into(ivPreviewMedia);
                ivClearPickedMedia.setVisibility(View.VISIBLE);
            }
        }

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBack();
            }
        });

        ivAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPhotoPicker();
            }
        });

        ivAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openVideoPicker();
            }
        });

        ivClearPickedMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearPreviewMedia();
            }
        });

        tvToolbarConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPost();
            }
        });
    }

    @Override
    public void onBackPressed() {
        onClickBack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE && resultCode == RESULT_OK && data != null) {
            clearPreviewMedia();
            fileUploadPath = Matisse.obtainPathResult(data).get(0);
            fileUploadType = "image";
            Glide.with(PostCreatorActivity.this)
                    .asBitmap()
                    .load(fileUploadPath)
                    .into(ivPreviewMedia);
            ivClearPickedMedia.setVisibility(View.VISIBLE);
        } else if (requestCode == RequestCodeResultActivity.REQUEST_CODE_CHOOSE_VIDEO && resultCode == RESULT_OK && data != null) {
            clearPreviewMedia();
            fileUploadPath = Matisse.obtainPathResult(data).get(0);
            fileUploadType = "video";
            RequestOptions options = new RequestOptions().frame(1000);
            Glide.with(PostCreatorActivity.this)
                    .load(fileUploadPath)
                    .apply(options)
                    .into(ivPreviewMedia);
            ivPlayVideo.setVisibility(View.VISIBLE);
            ivClearPickedMedia.setVisibility(View.VISIBLE);
        }
    }

    private void uploadPost() {
        ContextExtension.hideKeyboard(PostCreatorActivity.this);
        String caption = etContentPost.getText().toString().trim();
        if (checkValidValueString(caption)) {
            // call api upload post
            progressDialog.show();

            // if has media
            if (checkValidValueString(fileUploadPath) && checkValidValueString(fileUploadType)) {
                File file = new File(fileUploadPath);
                MultipartBody.Part mediaUpload = null;
                RequestBody type = null;
                if (fileUploadType.equals("image")) {
                    RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
                    mediaUpload = MultipartBody.Part.createFormData("media", file.getName(), requestFile);
                    type = RequestBody.create(FileExtension.MEDIA_IMAGE_TYPE, okhttp3.MultipartBody.FORM);
                } else if (fileUploadType.equals("video")) {
                    RequestBody requestFile = RequestBody.create(file, MediaType.parse("video/*"));
                    mediaUpload = MultipartBody.Part.createFormData("media", file.getName(), requestFile);
                    type = RequestBody.create(FileExtension.MEDIA_VIDEO_TYPE, okhttp3.MultipartBody.FORM);
                }

                Call<BaseResponse<Media>> call = uploadMediaService.uploadMedia(mediaUpload, type);
                call.enqueue(new Callback<BaseResponse<Media>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<Media>> call, Response<BaseResponse<Media>> response) {
                        if (response.isSuccessful()) {
                            BaseResponse<Media> res = response.body();
                            Media media = res.getData();
                            uploadPostWithMedia(caption, media);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(PostCreatorActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<Media>> call, Throwable t) {
                        // error network (no internet connection, socket timeout, unknown host, ...)
                        // error serializing/deserializing the data
                        call.cancel();
                        progressDialog.dismiss();
                        Toast.makeText(PostCreatorActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                uploadPostWithMedia(caption, null);
            }

        } else {
            Toast.makeText(PostCreatorActivity.this, R.string.error_post_creator_empty, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPostWithMedia(String caption, Media media) {
        Map<String, Object> req = new HashMap<>();
        req.put("caption", caption);
        req.put("type", 1); // type = 1 -> bài viết cá nhân
        if (media != null) {
            req.put("media_id", media.getId());
        }

        Call<BaseResponse<Post>> call = postService.createPost(req);
        call.enqueue(new Callback<BaseResponse<Post>>() {
            @Override
            public void onResponse(Call<BaseResponse<Post>> call, Response<BaseResponse<Post>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Post> res = response.body();
                    Post newPost = res.getData();

                    // create a notification
                    NotificationExtension.showPostSuccess(PostCreatorActivity.this);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("new_post", newPost);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(PostCreatorActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Post>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                progressDialog.dismiss();
                Toast.makeText(PostCreatorActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPhotoPicker() {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (granted) {
                        MediaPicker.openImagePicker(PostCreatorActivity.this, RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE, 1, true);
                    } else {
                        Toast.makeText(PostCreatorActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openVideoPicker() {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        MediaPicker.openVideoPicker(PostCreatorActivity.this, RequestCodeResultActivity.REQUEST_CODE_CHOOSE_VIDEO, 1);
                    } else {
                        Toast.makeText(PostCreatorActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearPreviewMedia() {
        if (checkValidValueString(fileUploadPath)) {
            fileUploadPath = "";
            fileUploadType = "";
            Glide.with(PostCreatorActivity.this)
                    .clear(ivPreviewMedia);
            ivPlayVideo.setVisibility(View.GONE);
            ivClearPickedMedia.setVisibility(View.GONE);
        }
    }

    private void onClickBack() {
        boolean isEditPost = etContentPost.getText().toString().trim().length() > 0;
        if (checkValidValueString(fileUploadPath)) {
            isEditPost = true;
        }
        if (isEditPost) {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PostCreatorActivity.this, R.style.BottomSheetDialogTheme);
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_post_creator_back);

            ConstraintLayout lDeletePost = bottomSheetDialog.findViewById(R.id.l_delete_post);
            ConstraintLayout lContinuePost = bottomSheetDialog.findViewById(R.id.l_continue_post);

            if (lDeletePost != null && lContinuePost != null) {
                lDeletePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

                lContinuePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });
            }

            bottomSheetDialog.show();
        } else {
            finish();
        }
    }
}