package vn.hust.socialnetwork.ui.groupcreator.crop;

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

import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.utils.ContextExtension;
import vn.hust.socialnetwork.utils.FileExtension;

public class CropGroupCoverActivity extends AppCompatActivity {

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle, tvToolbarConfirm;
    private ImageCropView icvCropImageCoverGroup;

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_group_cover);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imagePath = extras.getString("image_path");
        } else {
            imagePath = "";
            Toast.makeText(CropGroupCoverActivity.this, R.string.error_load_media, Toast.LENGTH_SHORT).show();
        }

        ivToolbarBack = findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        tvToolbarConfirm = findViewById(R.id.tv_toolbar_confirm);
        icvCropImageCoverGroup = findViewById(R.id.icv_crop_image_cover_group);

        tvToolbarTitle.setText(R.string.toolbar_title_crop_image);
        tvToolbarConfirm.setText(R.string.toolbar_text_right_edit_confirm);

        // set path to image crop
        if (!imagePath.isEmpty()) {
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
                    Bitmap croppedImage = icvCropImageCoverGroup.getCroppedImage();
                    if (croppedImage != null) {
                        Dialog progressDialog = ContextExtension.createProgressDialog(CropGroupCoverActivity.this);
                        progressDialog.show();

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                // save cropped image and return result to group creator activity
                                String croppedImagePath = bitmapConvertToFile(croppedImage);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        if (!croppedImagePath.isEmpty()) {
                                            Intent returnIntent = new Intent();
                                            returnIntent.putExtra("cropped_image_path", croppedImagePath);
                                            setResult(Activity.RESULT_OK, returnIntent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        });
                    } else {
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
}