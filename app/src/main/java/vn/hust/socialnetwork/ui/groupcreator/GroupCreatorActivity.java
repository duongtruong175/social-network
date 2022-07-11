package vn.hust.socialnetwork.ui.groupcreator;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tbruyelle.rxpermissions3.RxPermissions;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;
import java.util.List;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.ui.groupcreator.crop.CropGroupCoverActivity;
import vn.hust.socialnetwork.ui.mediaviewer.MediaViewerActivity;
import vn.hust.socialnetwork.utils.FileExtension;
import vn.hust.socialnetwork.utils.MediaPicker;
import vn.hust.socialnetwork.utils.RequestCodeResultActivity;

public class GroupCreatorActivity extends AppCompatActivity {

    private AppCompatImageView ivCreateGroupClose, ivCreateGroupChangeImageCover, ivImageCoverGroup;

    private String capturePhotoPath; // file path for capturing photo
    private String croppedImagePath; // file path of cropped image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creator);

        ivCreateGroupClose = findViewById(R.id.iv_create_group_close);
        ivCreateGroupChangeImageCover = findViewById(R.id.iv_create_group_change_image_cover);
        ivImageCoverGroup = findViewById(R.id.iv_image_cover_group);

        // init
        capturePhotoPath = "";
        croppedImagePath = "";

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodeResultActivity.REQUEST_CODE_CHOOSE_IMAGE && resultCode == RESULT_OK && data != null) {
            // Put selected image to Crop Activity
            List<String> s = Matisse.obtainPathResult(data);
            String s1 = Matisse.obtainPathResult(data).get(0);
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
            if (croppedImagePath.isEmpty()) {
                lViewPhoto.setVisibility(View.GONE);
            } else {
                lViewPhoto.setVisibility(View.VISIBLE);
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