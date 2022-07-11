package vn.hust.socialnetwork.ui.postdetail.adapters;

import android.view.View;

public interface OnCommentListener {
    void onUserCommentClick(int position);
    void onMediaItemClick(int position);
    void onReactCountClick(int position);
    void onReactActionClick(int reactType, int position);
    void onReactActionLongClick(int reactType, int position);
    void onReplyActionClick(int position);
    void onItemLongClick(View view, int position);
}
