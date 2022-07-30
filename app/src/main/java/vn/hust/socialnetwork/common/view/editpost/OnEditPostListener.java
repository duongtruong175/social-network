package vn.hust.socialnetwork.common.view.editpost;

import vn.hust.socialnetwork.models.post.Post;

public interface OnEditPostListener {
    void onConfirmClick(Post updatedPost);
}
