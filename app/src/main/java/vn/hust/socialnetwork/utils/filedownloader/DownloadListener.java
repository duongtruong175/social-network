package vn.hust.socialnetwork.utils.filedownloader;

public interface DownloadListener {
    void onSuccess(String dataPath);

    void onFailed(String message);

    void onPaused(String message);

    void onPending(String message);

    void onBusy();
}
