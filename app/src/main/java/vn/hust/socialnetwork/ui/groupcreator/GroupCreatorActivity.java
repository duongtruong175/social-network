package vn.hust.socialnetwork.ui.groupcreator;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.orhanobut.hawk.Hawk;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.style.CustomTypefaceSpan;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.media.Media;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.GroupService;
import vn.hust.socialnetwork.network.UploadMediaService;
import vn.hust.socialnetwork.ui.groupcreator.crop.CropGroupCoverActivity;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.NotificationExtension;
import vn.hust.socialnetwork.utils.RequestCodeResultActivity;

public class GroupCreatorActivity extends AppCompatActivity {

    private AppCompatImageView ivCreateGroupClose, ivCreateGroupChangeImageCover, ivImageCoverGroup;
    private TextInputEditText etNameGroup, etIntroductionGroup;
    private AppCompatTextView tvErrorNameGroup;
    private RadioGroup rgType;
    private AppCompatRadioButton rbPublic, rbPrivate;
    private AppCompatButton btnCreateGroup;
    private Dialog progressDialog;

    private GroupService groupService;
    private UploadMediaService uploadMediaService;

    private String capturePhotoPath; // file path for capturing photo
    private String croppedImagePath; // file path of cropped image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creator);
        // api
        groupService = ApiClient.getClient().create(GroupService.class);
        uploadMediaService = ApiClient.getClient().create(UploadMediaService.class);

        // binding
        ivCreateGroupClose = findViewById(R.id.iv_create_group_close);
        ivCreateGroupChangeImageCover = findViewById(R.id.iv_create_group_change_image_cover);
        ivImageCoverGroup = findViewById(R.id.iv_image_cover_group);
        etNameGroup = findViewById(R.id.et_name_group);
        tvErrorNameGroup = findViewById(R.id.tv_error_name_group);
        etIntroductionGroup = findViewById(R.id.et_introduction_group);
        rgType = findViewById(R.id.rg_type);
        rbPublic = findViewById(R.id.rb_public);
        rbPrivate = findViewById(R.id.rb_private);
        btnCreateGroup = findViewById(R.id.btn_create_group);

        // init
        progressDialog = ContextExtension.createProgressDialog(GroupCreatorActivity.this);
        capturePhotoPath = "";
        croppedImagePath = "";

        // init view
        Typeface typeface = ResourcesCompat.getFont(GroupCreatorActivity.this, R.font.f_roboto_medium);
        SpannableStringBuilder textPublic = new SpannableStringBuilder();
        textPublic.append(getString(R.string.group_type_public));
        textPublic.setSpan(new CustomTypefaceSpan(typeface), 0, textPublic.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textPublic.append("\n");
        int i = textPublic.length();
        textPublic.append(getString(R.string.create_group_hint_public_type));
        textPublic.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_size_small)), i, textPublic.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textPublic.setSpan(new ForegroundColorSpan(ContextCompat.getColor(GroupCreatorActivity.this, R.color.color_text_secondary)), i, textPublic.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        rbPublic.setText(textPublic);

        SpannableStringBuilder textPrivate = new SpannableStringBuilder();
        textPrivate.append(getString(R.string.group_type_private));
        textPrivate.setSpan(new CustomTypefaceSpan(typeface), 0, textPrivate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textPrivate.append("\n");
        int j = textPrivate.length();
        textPrivate.append(getString(R.string.create_group_hint_private_type));
        textPrivate.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_size_small)), j, textPrivate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textPrivate.setSpan(new ForegroundColorSpan(ContextCompat.getColor(GroupCreatorActivity.this, R.color.color_text_secondary)), j, textPrivate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        rbPrivate.setText(textPrivate);

        // fix scroll edit text
        etIntroductionGroup.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_SCROLL:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                }
                return false;
            }
        });

        ivCreateGroupClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ivCreateGroupChangeImageCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeCoverBottomSheetDialog();
            }
        });

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE && resultCode == RESULT_OK && data != null) {
            // Put selected image to Crop Activity
            Intent intent = new Intent(GroupCreatorActivity.this, CropGroupCoverActivity.class);
            intent.putExtra("image_path", Matisse.obtainPathResult(data).get(0));
            cropImageActivityResultLauncher.launch(intent);
        }
        if (requestCode == RequestCodeResultActivity.REQUEST_CODE_CAPTURE_PHOTO && resultCode == RESULT_OK && !capturePhotoPath.isEmpty()) {
            // Put captured image to Crop Activity
            Intent intent = new Intent(GroupCreatorActivity.this, CropGroupCoverActivity.class);
            intent.putExtra("image_path", capturePhotoPath);
            cropImageActivityResultLauncher.launch(intent);
        }
    }

    // ActivityResultLauncher for crop image activity
    ActivityResultLauncher<Intent> cropImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get image from result
                        Intent data = result.getData();
                        if (data != null) {
                            croppedImagePath = data.getStringExtra("cropped_image_path");
                            Glide.with(GroupCreatorActivity.this)
                                    .clear(ivImageCoverGroup);
                            Glide.with(GroupCreatorActivity.this)
                                    .asBitmap()
                                    .load(croppedImagePath)
                                    .into(ivImageCoverGroup);
                        }
                    }
                }
            });

    private void createGroup() {
        ContextExtension.hideKeyboard(GroupCreatorActivity.this);
        tvErrorNameGroup.setVisibility(View.GONE);
        tvErrorNameGroup.setText("");

        String name = etNameGroup.getText().toString().trim();
        if (name.isEmpty()) {
            tvErrorNameGroup.setVisibility(View.VISIBLE);
            tvErrorNameGroup.setText(R.string.error_name_group_validation);
            ContextExtension.showKeyboard(etNameGroup);
        } else {
            progressDialog.show();
            // if has media
            if (checkValidValueString(croppedImagePath)) {
                File file = new File(croppedImagePath);
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
                            createGroupWithMedia(media);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(GroupCreatorActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<Media>> call, Throwable t) {
                        // error network (no internet connection, socket timeout, unknown host, ...)
                        // error serializing/deserializing the data
                        call.cancel();
                        progressDialog.dismiss();
                        Toast.makeText(GroupCreatorActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                createGroupWithMedia(null);
            }
        }
    }

    private void createGroupWithMedia(Media media) {
        String name = etNameGroup.getText().toString().trim();
        String introduction = etIntroductionGroup.getText().toString().trim();
        int rbSelectedId = rgType.getCheckedRadioButtonId();
        int type = 1; // default 1 -> public
        if (rbSelectedId == R.id.rb_public) {
            type = 1;
        } else if (rbSelectedId == R.id.rb_private) {
            type = 2;
        }
        int adminId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);

        Map<String, Object> req = new HashMap<>();
        req.put("admin_id", adminId);
        req.put("name", name);
        req.put("type", type);
        if (media != null) {
            req.put("cover_image", media.getSrc());
        }
        if (!introduction.isEmpty()) {
            req.put("introduction", introduction);
        }
        Call<BaseResponse<Group>> call = groupService.createGroup(req);
        call.enqueue(new Callback<BaseResponse<Group>>() {
            @Override
            public void onResponse(Call<BaseResponse<Group>> call, Response<BaseResponse<Group>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Group> res = response.body();
                    Group newGroup = res.getData();

                    // create a notification
                    NotificationExtension.showCreateGroupSuccess(GroupCreatorActivity.this);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("new_group", newGroup);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(GroupCreatorActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Group>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                progressDialog.dismiss();
                Toast.makeText(GroupCreatorActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show a bottom sheet dialog for change cover
     *
     * @return None
     */
    private void showChangeCoverBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GroupCreatorActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_cover_change);

        ConstraintLayout lViewPhoto = bottomSheetDialog.findViewById(R.id.l_view_photo);
        ConstraintLayout lTakePhoto = bottomSheetDialog.findViewById(R.id.l_take_photo);
        ConstraintLayout lChangePhoto = bottomSheetDialog.findViewById(R.id.l_change_photo);

        if (lViewPhoto != null && lTakePhoto != null && lChangePhoto != null) {
            if (checkValidValueString(croppedImagePath)) {
                lViewPhoto.setVisibility(View.VISIBLE);
            } else {
                lViewPhoto.setVisibility(View.GONE);
            }

            lViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    if (!croppedImagePath.isEmpty()) {
                        Intent viewPhotoIntent = new Intent(GroupCreatorActivity.this, MediaViewerActivity.class);
                        viewPhotoIntent.putExtra("media_url", croppedImagePath);
                        viewPhotoIntent.putExtra("media_type", FileExtension.MEDIA_IMAGE_TYPE);
                        startActivity(viewPhotoIntent);
                    }
                }
            });

            lTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    RxPermissions rxPermissions = new RxPermissions(GroupCreatorActivity.this);
                    rxPermissions.request(Manifest.permission.CAMERA)
                            .subscribe(granted -> {
                                if (granted) {
                                    try {
                                        File capturePhotoFile = FileExtension.getPathCapturePhoto(GroupCreatorActivity.this);
                                        capturePhotoPath = capturePhotoFile.getAbsolutePath();
                                        MediaPicker.openCameraForCapturePhoto(GroupCreatorActivity.this, RequestCodeResultActivity.REQUEST_CODE_CAPTURE_PHOTO, capturePhotoFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(GroupCreatorActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

            lChangePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    RxPermissions rxPermissions = new RxPermissions(GroupCreatorActivity.this);
                    rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(granted -> {
                                if (granted) {
                                    MediaPicker.openCoverPicker(GroupCreatorActivity.this, RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE, 1, false);
                                } else {
                                    Toast.makeText(GroupCreatorActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }

        bottomSheetDialog.show();
    }
}