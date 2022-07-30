package vn.hust.socialnetwork.common.view.editcomment;

import vn.hust.socialnetwork.models.post.CommentPost;

public interface OnEditCommentListener {
    void onConfirmClick(CommentPost updatedComment);
}
