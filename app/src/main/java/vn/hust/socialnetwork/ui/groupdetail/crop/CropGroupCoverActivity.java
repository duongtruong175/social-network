package vn.hust.socialnetwork.ui.groupdetail.crop;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.userprofile.ChangeCoverImageResponse;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.GroupService;
import vn.hust.socialnetwork.ui.main.userprofile.crop.CropUserCoverActivity;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;

public class CropGroupCoverActivity extends AppCompatActivity {

    private GroupService groupService;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private ImageCropView icvCropImageCoverGroup;
    private Dialog progressDialog;

    private int groupId;
    private String imagePath;
    private String croppedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_group_cover);

        // api
        groupService = ApiClient.getClient().create(GroupService.class);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imagePath = extras.getString("image_path");
            groupId = extras.getInt("group_id");
        } else {
            imagePath = "";
            groupId = 0;
            Toast.makeText(CropGroupCoverActivity.this, R.string.error_load_media, Toast.LENGTH_SHORT).show();
        }

        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = findViewById(R.id.tv_toolbar_confirm);
        icvCropImageCoverGroup = findViewById(R.id.icv_crop_image_cover_group);

        progressDialog = ContextExtension.createProgressDialog(CropGroupCoverActivity.this);
        tvToolbarTitle.setText(R.string.toolbar_title_crop_image);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // set path to image crop
        if (checkValidValueString(imagePath)) {
            icvCropImageCoverGroup.setImageFilePath(imagePath);
            icvCropImageCoverGroup.setAspectRatio(18, 10);
        }

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
                if (!icvCropImageCoverGroup.isChangingScale()) {
                    progressDialog.show();
                    Bitmap croppedImage = icvCropImageCoverGroup.getCroppedImage();
                    if (croppedImage != null) {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                // save cropped image and return result to group creator activity
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
                        Toast.makeText(CropGroupCoverActivity.this, R.string.fail_to_crop, Toast.LENGTH_SHORT).show();
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
            File pathSavePhoto = FileExtension.getPathSavePhoto(CropGroupCoverActivity.this);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(pathSavePhoto, false));
            filePath = pathSavePhoto.getAbsolutePath();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return filePath;
        }
    }

    private void changeCoverImage() {
        if (checkValidValueString(croppedImagePath) && groupId != 0) {
            File file = new File(croppedImagePath);
            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part mediaUpload = MultipartBody.Part.createFormData("cover_image", file.getName(), requestFile);
            Call<BaseResponse<String>> call = groupService.updateCoverImageGroup(mediaUpload, groupId);
            call.enqueue(new Callback<BaseResponse<String>>() {
                @Override
                public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<String> res = response.body();
                        String newCoverImage = res.getData();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("new_cover_image", newCoverImage);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } else {
                        Toast.makeText(CropGroupCoverActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    progressDialog.dismiss();
                    Toast.makeText(CropGroupCoverActivity.this, R.string.error_call_api_failure, Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            progressDialog.dismiss();
            Toast.makeText(CropGroupCoverActivity.this, R.string.error_crop_image_failure, Toast.LENGTH_SHORT).show();
        }
    }
}