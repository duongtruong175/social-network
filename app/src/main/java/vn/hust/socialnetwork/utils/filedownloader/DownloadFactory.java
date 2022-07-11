package vn.hust.socialnetwork.utils.filedownloader;

import android.content.Context;

public final class DownloadFactory {
    private Context context;
    private String downloadFileUrl, fileName;

    public DownloadFactory(Context context, String downloadFileUrl, String fileName) {
        this.context = context;
        this.downloadFileUrl = downloadFileUrl;
        this.fileName = fileName;
    }

    public DownloadFile downloadFile(FILE_TYPE fileType) {
        switch (fileType) {
            case IMAGE: {
                return new DownloadImage(context, downloadFileUrl, fileName);
            }
            case VIDEO: {
                return new DownloadVideo(context, downloadFileUrl, fileName);
            }
        }
        return null;
    }
}
