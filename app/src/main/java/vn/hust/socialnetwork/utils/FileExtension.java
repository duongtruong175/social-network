package vn.hust.socialnetwork.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileExtension {
    public static final String MEDIA_IMAGE_TYPE = "image";
    public static final String MEDIA_VIDEO_TYPE = "video";
    // folder for download media (Download/)
    public static final String IMAGE_FOLDER_DOWNLOAD = "SocialNetwork/Images";
    public static final String VIDEO_FOLDER_DOWNLOAD = "SocialNetwork/Videos";
    // folder for saving edited photo (Android/data/android_id/files/Pictures/)
    public static final String PHOTO_EDITOR_FOLDER_SAVED = "Edit_Photo";
    public static final String CAPTURE_PHOTO_FOLDER_SAVED = "Capture_Photo";
    public static final String SCREENSHOT_LAYOUT_FOLDER_SAVED = "Screenshot_Photo";

    /**
     * Create a File for saving edited photo.
     * Folder will be delete when uninstall app.
     *
     * @return Return a File with name is PNG_timestamp.png
     */
    public static File getPathSavePhoto(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp;
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String sb = externalFilesDir.getAbsolutePath() +
                File.separator +
                PHOTO_EDITOR_FOLDER_SAVED +
                File.separator +
                imageFileName +
                ".png";
        File pathSavePhoto = new File(sb);
        if (!pathSavePhoto.exists()) {
            pathSavePhoto.mkdir();
        }
        if (!pathSavePhoto.getParentFile().exists()) {
            pathSavePhoto.getParentFile().mkdirs();
        }
        if (pathSavePhoto.exists()) {
            pathSavePhoto.delete();
        }
        pathSavePhoto.createNewFile();
        return pathSavePhoto;
    }

    /**
     * Create a File for saving cropped photo.
     * Folder will be delete when uninstall app.
     *
     * @return Return a File with name is JPEG_timestamp_(random_string).jpg
     */
    public static File getPathSaveCropPhoto(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String sb = externalFilesDir.getAbsolutePath() +
                File.separator +
                PHOTO_EDITOR_FOLDER_SAVED;
        File capturePhotoFolder = new File(sb);
        if (!capturePhotoFolder.exists()) {
            capturePhotoFolder.mkdirs();
        }
        File pathCapturePhoto = File.createTempFile(imageFileName, ".jpg", capturePhotoFolder);
        return pathCapturePhoto;
    }

    /**
     * Create a File for saving capture photo.
     * Folder will be delete when uninstall app.
     *
     * @return Return a File with name is JPEG_timestamp_(random_string).jpg
     */
    public static File getPathCapturePhoto(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String sb = externalFilesDir.getAbsolutePath() +
                File.separator +
                CAPTURE_PHOTO_FOLDER_SAVED;
        File capturePhotoFolder = new File(sb);
        if (!capturePhotoFolder.exists()) {
            capturePhotoFolder.mkdirs();
        }
        File pathCapturePhoto = File.createTempFile(imageFileName, ".jpg", capturePhotoFolder);
        return pathCapturePhoto;
    }

    /**
     * Create a File for screen layout content.
     * Folder will be delete when uninstall app.
     *
     * @return Return a File with name is JPEG_timestamp_(random_string).jpg
     */
    public static File getPathScreenPhoto(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String sb = externalFilesDir.getAbsolutePath() +
                File.separator +
                SCREENSHOT_LAYOUT_FOLDER_SAVED;
        File capturePhotoFolder = new File(sb);
        if (!capturePhotoFolder.exists()) {
            capturePhotoFolder.mkdirs();
        }
        File pathCapturePhoto = File.createTempFile(imageFileName, ".jpg", capturePhotoFolder);
        return pathCapturePhoto;
    }
}
