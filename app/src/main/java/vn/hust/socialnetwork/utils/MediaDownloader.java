package vn.hust.socialnetwork.utils;

import android.content.Context;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.utils.filedownloader.DownloadFactory;
import vn.hust.socialnetwork.utils.filedownloader.DownloadFile;
import vn.hust.socialnetwork.utils.filedownloader.DownloadListener;
import vn.hust.socialnetwork.utils.filedownloader.FILE_TYPE;

public class MediaDownloader {

    private static final List<String> IMAGE_FILE_EXTENSION = new ArrayList<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));
    private static final List<String> VIDEO_FILE_EXTENSION = new ArrayList<>(Arrays.asList(".mkv", ".mp4", ".mpeg", ".mov", ".3gp", ".avi", ".webm"));
    private Context context;

    public MediaDownloader(Context context) {
        this.context = context;
    }

    /**
     * Download file from url
     */
    public void download(String url) {
        FILE_TYPE fileType = getFileType(url);
        if (fileType == null) {
            Toast.makeText(this.context, R.string.file_download_not_support, Toast.LENGTH_SHORT).show();
            return;
        }
        onStartDownload(fileType);
        // only support download with http/https
        // because using class DownloadManager
        if (!url.startsWith("http")) {
            onDownloadFail(fileType);
            return;
        }
        DownloadFactory downloadFactory = new DownloadFactory(this.context, url, getFileNameFromURL(url));
        DownloadFile downloadFile = downloadFactory.downloadFile(fileType);
        if (downloadFile != null) {
            downloadFile.start(new DownloadListener() {
                @Override
                public void onSuccess(String dataPath) {
                    if (dataPath != null && !dataPath.isEmpty()) {
                        onSuccessDownload(fileType);
                    }
                }

                @Override
                public void onFailed(String message) {
                    onDownloadFail(fileType);
                }

                @Override
                public void onPaused(String message) {

                }

                @Override
                public void onPending(String message) {

                }

                @Override
                public void onBusy() {

                }
            });
        }
    }

    private void onStartDownload(FILE_TYPE fileType) {
        if (fileType == FILE_TYPE.IMAGE) {
            Toast.makeText(this.context, R.string.photo_downloading, Toast.LENGTH_SHORT).show();
        } else if (fileType == FILE_TYPE.VIDEO) {
            Toast.makeText(this.context, R.string.video_downloading, Toast.LENGTH_SHORT).show();
        }
    }

    private void onSuccessDownload(FILE_TYPE fileType) {
        if (fileType == FILE_TYPE.IMAGE) {
            Toast.makeText(this.context, R.string.photo_downloaded, Toast.LENGTH_SHORT).show();
        } else if (fileType == FILE_TYPE.VIDEO) {
            Toast.makeText(this.context, R.string.video_downloaded, Toast.LENGTH_SHORT).show();
        }
    }

    private void onDownloadFail(FILE_TYPE fileType) {
        if (fileType == FILE_TYPE.IMAGE) {
            Toast.makeText(this.context, R.string.photo_downloaded_error, Toast.LENGTH_SHORT).show();
        } else if (fileType == FILE_TYPE.VIDEO) {
            Toast.makeText(this.context, R.string.video_downloaded_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get a file type from a url
     */
    private FILE_TYPE getFileType(String url) {
        String extension = url.substring(url.lastIndexOf('.'), url.length());
        if (IMAGE_FILE_EXTENSION.contains(extension.toLowerCase())) {
            return FILE_TYPE.IMAGE;
        }
        if (VIDEO_FILE_EXTENSION.contains(extension.toLowerCase())) {
            return FILE_TYPE.VIDEO;
        }
        return null;
    }

    /**
     * Get a file name from a url
     */
    public String getFileNameFromURL(String url) {
        try {
            String fileName = URLUtil.guessFileName(url, null, null);
            return fileName;
        } catch (Exception unused) {
            return String.valueOf(System.currentTimeMillis()) + getFileType(url);
        }
    }
}
