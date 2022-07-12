package vn.hust.socialnetwork.ui.main.feed.adapters;

import android.view.View;

public interface OnPostListener {
    void onUserPostClick(int position);
    void onGroupPostClick(int position);
    void onMenuItemClick(int position);
    void onContentLongClick(int position);
    void onMediaItemClick(int position);
    void onReactCountClick(int position);
    void onReactActionClick(int reactType, int position);
    void onReactActionLongClick(int reactType, int position);
    void onCommentActionClick(int position);
    void onShareActionClick(View lContentToShare, int position);
    void onItemClick(int position);
}