package vn.hust.socialnetwork.common.view.commentpost.adapters;

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
