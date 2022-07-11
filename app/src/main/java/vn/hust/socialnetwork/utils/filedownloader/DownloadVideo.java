package vn.hust.socialnetwork.utils.filedownloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.utils.FileExtension;

public class DownloadVideo implements DownloadFile {

    private long downloadReference;
    private BroadcastReceiver receiverDownloadComplete;
    DownloadManager downloadManager;
    String downloadFileUrl;
    private Context context;
    private String fileName;

    public DownloadVideo(Context context, String downloadFileUrl, String fileName) {
        this.context = context;
        this.downloadFileUrl = downloadFileUrl;
        this.fileName = fileName;
    }

    @Override
    public void start(DownloadListener downloadListener) {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadFileUrl);

        String folderDownload = Environment.DIRECTORY_DOWNLOADS;
        String pathFile = File.separator
                + FileExtension.VIDEO_FOLDER_DOWNLOAD
                + File.separator
                + fileName;

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription(context.getString(R.string.status_downloading_dw))
                .setTitle(context.getString(R.string.video_file_title_dw))
                .setDestinationInExternalPublicDir(folderDownload, pathFile)
                .setVisibleInDownloadsUi(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

        downloadReference = downloadManager.enqueue(request);

        /**
         * filter for download - on completion
         */
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        receiverDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadReference == reference) {
                    // do something with download file
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    Cursor cursor = downloadManager.query(query);

                    /**
                     * get the status of download.
                     */
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

                    if (cursor.moveToFirst()) {
                        int status = cursor.getInt(columnIndex);
                        int fileNameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        String savedFilePath = cursor.getString(fileNameIndex);

                        /**
                         * get the reason-more detail on the status.
                         */
                        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                        int reason = cursor.getInt(columnReason);

                        switch (status) {
                            case DownloadManager.STATUS_SUCCESSFUL:
                                if (downloadListener != null) {
                                    downloadListener.onSuccess(savedFilePath);
                                }
                                break;
                            case DownloadManager.STATUS_FAILED:
                                if (downloadListener != null) {
                                    downloadListener.onFailed(String.valueOf(reason));
                                }
                                break;
                            case DownloadManager.STATUS_PAUSED:
                                if (downloadListener != null) {
                                    downloadListener.onPaused(String.valueOf(reason));
                                }
                                break;
                            case DownloadManager.STATUS_PENDING:
                                if (downloadListener != null) {
                                    downloadListener.onPending(String.valueOf(reason));
                                }
                                break;
                        }
                        cursor.close();
                    }
                }
            }
        };
        context.registerReceiver(receiverDownloadComplete, intentFilter);
    }
}
