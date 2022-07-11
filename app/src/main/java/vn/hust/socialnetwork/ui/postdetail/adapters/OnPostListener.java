package vn.hust.socialnetwork.ui.postdetail.adapters;

import android.view.View;

public interface OnPostListener {
    void onUserPostClick();
    void onGroupPostClick();
    void onMenuItemClick();
    void onContentLongClick();
    void onMediaItemClick();
    void onReactCountClick();
    void onReactActionClick(int reactType);
    void onReactActionLongClick(int reactType);
    void onShareActionClick(View lContentToShare);
}
