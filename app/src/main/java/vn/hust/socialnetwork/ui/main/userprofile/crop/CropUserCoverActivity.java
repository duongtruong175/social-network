package vn.hust.socialnetwork.ui.main.userprofile.crop;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.userprofile.ChangeAvatarResponse;
import vn.hust.socialnetwork.models.userprofile.ChangeCoverImageResponse;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;

public class CropUserCoverActivity extends AppCompatActivity {

    private UserProfileService userProfileService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private ImageCropView icvCropImageCoverUser;
    private CircleImageView civImageAvatar;
    private Dialog progressDialog;

    private String imagePath, imageAvatar;
    private String croppedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_user_cover);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imagePath = extras.getString("image_path");
            imageAvatar = extras.getString("image_avatar");
        } else {
            imagePath = "";
            imageAvatar = "";
            Toast.makeText(CropUserCoverActivity.this, R.string.error_load_media, Toast.LENGTH_SHORT).show();
        }

        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = findViewById(R.id.tv_toolbar_confirm);
        icvCropImageCoverUser = findViewById(R.id.icv_crop_image_cover_user);
        civImageAvatar = findViewById(R.id.civ_image_avatar);

        progressDialog = ContextExtension.createProgressDialog(CropUserCoverActivity.this);
        tvToolbarTitle.setText(R.string.toolbar_title_crop_image);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // set path to image crop
        if (checkValidValueString(imagePath)) {
            icvCropImageCoverUser.setImageFilePath(imagePath);
            icvCropImageCoverUser.setAspectRatio(18, 10);
        }
        Glide.with(CropUserCoverActivity.this)
                .asBitmap()
                .load(imageAvatar)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(civImageAvatar);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        tvToolbarConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!icvCropImageCoverUser.isChangingScale()) {
                    progressDialog.show();
                    Bitmap croppedImage = icvCropImageCoverUser.getCroppedImage();
                    if (croppedImage != null) {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                // save cropped image and return result to activity
                                croppedImagePath = bitmapConvertToFile(croppedImage);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // call api cover image
                                        changeCoverImage();
                                    }
                                });
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(CropUserCoverActivity.this, R.string.fail_to_crop, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Convert and save bitmap (cropped image) to file
     *
     * @return Return a file path of saved photo
     */
    private String bitmapConvertToFile(Bitmap bitmap) {
        String filePath = "";
        try {
            File pathSavePhoto = FileExtension.getPathSavePhoto(CropUserCoverActivity.this);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(pathSavePhoto, false));
            filePath = pathSavePhoto.getAbsolutePath();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return filePath;
        }
    }

    private void changeCoverImage() {
        if (checkValidValueString(croppedImagePath)) {
            File file = new File(croppedImagePath);
            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part mediaUpload = MultipartBody.Part.createFormData("cover_image", file.getName(), requestFile);
            int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
            Call<BaseResponse<ChangeCoverImageResponse>> call = userProfileService.updateCoverImage(mediaUpload, userId);
            call.enqueue(new Callback<BaseResponse<ChangeCoverImageResponse>>() {
                @Override
                public void onResponse(Call<BaseResponse<ChangeCoverImageResponse>> call, Response<BaseResponse<ChangeCoverImageResponse>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<ChangeCoverImageResponse> res = response.body();
                        ChangeCoverImageResponse data = res.getData();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("new_cover_image", data.getCoverImage());
                        returnIntent.putExtra("new_post", data.getPost());
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } else {
                        Toast.makeText(CropUserCoverActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<BaseResponse<ChangeCoverImageResponse>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    progressDialog.dismiss();
                    Toast.makeText(CropUserCoverActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(CropUserCoverActivity.this, R.string.error_crop_image_failure, Toast.LENGTH_SHORT).show();
        }
    }
}