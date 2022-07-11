package vn.hust.socialnetwork.utils;

import android.app.Activity;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.provider.MediaStore;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;

import kotlin.jvm.internal.Intrinsics;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.utils.mediafilter.ImageSizeFilter;
import vn.hust.socialnetwork.utils.mediafilter.VideoSizeFilter;

public class MediaPicker {
    private static final int MIN_WIDTH_VIDEO_UPLOAD = 120;
    private static final int MIN_HEIGHT_VIDEO_UPLOAD = 120;
    private static final long MAX_SIZE_VIDEO_UPLOAD = 1073741824;
    private static final int MIN_WIDTH_IMAGE_UPLOAD = 100;
    private static final int MIN_HEIGHT_IMAGE_UPLOAD = 100;
    private static final int MIN_WIDTH_AVATAR_IMAGE_UPLOAD = 200;
    private static final int MIN_HEIGHT_AVATAR_IMAGE_UPLOAD = 200;
    private static final int MIN_WIDTH_COVER_IMAGE_UPLOAD = 400;
    private static final int MIN_HEIGHT_COVER_IMAGE_UPLOAD = 150;
    private static final long MAX_SIZE_IMAGE_UPLOAD = 104857600;

    public static void openImagePicker(Activity activity, int requestCode, int maxItemSelectable, boolean isCapture) {
        Matisse.from(activity)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .theme(R.style.Matisse_Custom)
                .maxSelectable(maxItemSelectable)
                .capture(isCapture)
                .captureStrategy(new CaptureStrategy(false, Intrinsics.stringPlus(activity.getPackageName(), ".fileprovider"), FileExtension.CAPTURE_PHOTO_FOLDER_SAVED))
                .addFilter(new ImageSizeFilter(MIN_WIDTH_IMAGE_UPLOAD, MIN_HEIGHT_IMAGE_UPLOAD, MAX_SIZE_IMAGE_UPLOAD))
                .gridExpectedSize(activity.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .showSingleMediaType(true)
                .thumbnailScale(0.85f)
                .autoHideToolbarOnSingleTap(true)
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }

    public static void openAvatarPicker(Activity activity, int requestCode, int maxItemSelectable, boolean isCapture) {
        Matisse.from(activity)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .theme(R.style.Matisse_Custom)
                .maxSelectable(maxItemSelectable)
                .capture(isCapture)
                .captureStrategy(new CaptureStrategy(false, Intrinsics.stringPlus(activity.getPackageName(), ".fileprovider"), FileExtension.CAPTURE_PHOTO_FOLDER_SAVED))
                .addFilter(new ImageSizeFilter(MIN_WIDTH_AVATAR_IMAGE_UPLOAD, MIN_HEIGHT_AVATAR_IMAGE_UPLOAD, MAX_SIZE_IMAGE_UPLOAD))
                .gridExpectedSize(activity.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .showSingleMediaType(true)
                .thumbnailScale(0.85f)
                .autoHideToolbarOnSingleTap(true)
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }

    public static void openAvatarPicker(Fragment fragment, int requestCode, int maxItemSelectable, boolean isCapture) {
        Matisse.from(fragment)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .theme(R.style.Matisse_Custom)
                .maxSelectable(maxItemSelectable)
                .capture(isCapture)
                .captureStrategy(new CaptureStrategy(false, Intrinsics.stringPlus(fragment.requireContext().getPackageName(), ".fileprovider"), FileExtension.CAPTURE_PHOTO_FOLDER_SAVED))
                .addFilter(new ImageSizeFilter(MIN_WIDTH_AVATAR_IMAGE_UPLOAD, MIN_HEIGHT_AVATAR_IMAGE_UPLOAD, MAX_SIZE_IMAGE_UPLOAD))
                .gridExpectedSize(fragment.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .showSingleMediaType(true)
                .thumbnailScale(0.85f)
                .autoHideToolbarOnSingleTap(true)
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }

    public static void openCoverPicker(Activity activity, int requestCode, int maxItemSelectable, boolean isCapture) {
        Matisse.from(activity)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .theme(R.style.Matisse_Custom)
                .maxSelectable(maxItemSelectable)
                .capture(isCapture)
                .captureStrategy(new CaptureStrategy(false, Intrinsics.stringPlus(activity.getPackageName(), ".fileprovider"), FileExtension.CAPTURE_PHOTO_FOLDER_SAVED))
                .addFilter(new ImageSizeFilter(MIN_WIDTH_COVER_IMAGE_UPLOAD, MIN_HEIGHT_COVER_IMAGE_UPLOAD, MAX_SIZE_IMAGE_UPLOAD))
                .gridExpectedSize(activity.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .showSingleMediaType(true)
                .thumbnailScale(0.85f)
                .autoHideToolbarOnSingleTap(true)
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }

    public static void openCoverPicker(Fragment fragment, int requestCode, int maxItemSelectable, boolean isCapture) {
        Matisse.from(fragment)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .theme(R.style.Matisse_Custom)
                .maxSelectable(maxItemSelectable)
                .capture(isCapture)
                .captureStrategy(new CaptureStrategy(false, Intrinsics.stringPlus(fragment.requireContext().getPackageName(), ".fileprovider"), FileExtension.CAPTURE_PHOTO_FOLDER_SAVED))
                .addFilter(new ImageSizeFilter(MIN_WIDTH_COVER_IMAGE_UPLOAD, MIN_HEIGHT_COVER_IMAGE_UPLOAD, MAX_SIZE_IMAGE_UPLOAD))
                .gridExpectedSize(fragment.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .showSingleMediaType(true)
                .thumbnailScale(0.85f)
                .autoHideToolbarOnSingleTap(true)
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }

    public static void openVideoPicker(Activity activity, int requestCode, int maxItemSelectable) {
        Matisse.from(activity)
                .choose(MimeType.ofVideo(), false)
                .countable(true)
                .theme(R.style.Matisse_Custom)
                .maxSelectable(maxItemSelectable)
                .addFilter(new VideoSizeFilter(MIN_WIDTH_VIDEO_UPLOAD, MIN_HEIGHT_VIDEO_UPLOAD, MAX_SIZE_VIDEO_UPLOAD))
                .gridExpectedSize(activity.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .showSingleMediaType(true)
                .thumbnailScale(0.85f)
                .autoHideToolbarOnSingleTap(true)
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }

    public static void openCameraForCapturePhoto(Activity activity, int requestCode, File capturePhotoFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(activity, Intrinsics.stringPlus(activity.getPackageName(), ".fileprovider"), capturePhotoFile));
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static void openCameraForCapturePhoto(Fragment fragment, int requestCode, File capturePhotoFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(fragment.requireContext().getPackageManager()) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(fragment.requireContext(), Intrinsics.stringPlus(fragment.requireContext().getPackageName(), ".fileprovider"), capturePhotoFile));
            fragment.startActivityForResult(intent, requestCode);
        }
    }
}
