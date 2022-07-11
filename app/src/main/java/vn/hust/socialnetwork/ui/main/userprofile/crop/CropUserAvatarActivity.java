package vn.hust.socialnetwork.ui.main.userprofile.crop;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.userprofile.ChangeAvatarResponse;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.UserProfileService;
import vn.hust.socialnetwork.utils.AppSharedPreferences;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;

public class CropUserAvatarActivity extends AppCompatActivity implements UCropFragmentCallback {

    private UserProfileService userProfileService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private Dialog progressDialog;

    private String imagePath, outputImagePath;

    private UCropFragment uCropFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_user_avatar);

        // api
        userProfileService = ApiClient.getClient().create(UserProfileService.class);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imagePath = extras.getString("image_path");
        } else {
            imagePath = "";
            Toast.makeText(CropUserAvatarActivity.this, R.string.error_load_media, Toast.LENGTH_SHORT).show();
        }

        // binding
        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = findViewById(R.id.tv_toolbar_confirm);

        progressDialog = ContextExtension.createProgressDialog(CropUserAvatarActivity.this);
        tvToolbarTitle.setText(R.string.toolbar_title_crop_image_avatar);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBack();
            }
        });

        tvToolbarConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uCropFragment != null) {
                    if (uCropFragment.isAdded()) {
                        // define result in call back below
                        uCropFragment.cropAndSaveImage();
                    }
                }
            }
        });

        // init crop
        // create a file to save
        try {
            createOutputCropFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initCropView();
    }

    @Override
    public void loadingProgress(boolean showLoader) {
        if (progressDialog != null) {
            if (showLoader) {
                progressDialog.show();
            } else {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onCropFinish(UCropFragment.UCropResult result) {
        if (result.mResultCode == RESULT_OK) {
            // handle success
            // imageCropPath has already passed in when init UCrop
            // call api change avatar
            changeAvatar();

        } else if (result.mResultCode == UCrop.RESULT_ERROR) {
            // handle error
            Throwable error = UCrop.getError(result.mResultData);
            if (error != null) {
                String message = error.getMessage();
                if (message.isEmpty()) {
                    Toast.makeText(CropUserAvatarActivity.this, R.string.invalid_extension, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CropUserAvatarActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CropUserAvatarActivity.this, R.string.general_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        onClickBack();
    }

    private void onClickBack() {
        // remove uCrop Fragment from Screen
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        beginTransaction.remove(uCropFragment)
                .commit();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void initCropView() {
        if (!imagePath.isEmpty() && !outputImagePath.isEmpty()) {
            // Params UCrop: input image path and output image path
            UCrop uCrop = UCrop.of(Uri.fromFile(new File(imagePath)), Uri.fromFile(new File(outputImagePath)))
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(1920, 1920);

            UCrop.Options options = new UCrop.Options();
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
            options.setCompressionQuality(100);
            options.setCircleDimmedLayer(true);
            options.setHideBottomControls(true);
            options.setFreeStyleCropEnabled(false);
            options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);
            options.setCropFrameColor(getColor(R.color.colorAccent));
            options.setShowCropGrid(false);

            uCrop.withOptions(options);

            // override UI of UCrop in ucrop_fragment_photobox.xml
            // binding custom UI UCrop to activity crop
            uCropFragment = uCrop.getFragment(uCrop.getIntent(CropUserAvatarActivity.this).getExtras());
            FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
            beginTransaction.add(R.id.fragment_container, uCropFragment, UCropFragment.TAG)
                    .commitAllowingStateLoss();
        } else {
            Toast.makeText(CropUserAvatarActivity.this, R.string.error_load_media, Toast.LENGTH_SHORT).show();
        }
    }

    private void createOutputCropFile() throws IOException {
        File pathSavePhoto = FileExtension.getPathSaveCropPhoto(CropUserAvatarActivity.this);
        outputImagePath = pathSavePhoto.getAbsolutePath();
    }

    private void changeAvatar() {
        if (checkValidValueString(outputImagePath)) {
            progressDialog.show();
            File file = new File(outputImagePath);
            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part mediaUpload = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
            int userId = Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0);
            Call<BaseResponse<ChangeAvatarResponse>> call = userProfileService.updateAvatar(mediaUpload, userId);
            call.enqueue(new Callback<BaseResponse<ChangeAvatarResponse>>() {
                @Override
                public void onResponse(Call<BaseResponse<ChangeAvatarResponse>> call, Response<BaseResponse<ChangeAvatarResponse>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<ChangeAvatarResponse> res = response.body();
                        ChangeAvatarResponse data = res.getData();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("new_avatar", data.getAvatar());
                        returnIntent.putExtra("new_post", data.getPost());
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } else {
                        Toast.makeText(CropUserAvatarActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<BaseResponse<ChangeAvatarResponse>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    progressDialog.dismiss();
                    Toast.makeText(CropUserAvatarActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}