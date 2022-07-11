package vn.hust.socialnetwork.models.userprofile;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import vn.hust.socialnetwork.models.post.Post;

public class ChangeAvatarResponse implements Parcelable {
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("post")
    @Expose
    private Post post;

    public ChangeAvatarResponse(String avatar, Post post) {
        this.avatar = avatar;
        this.post = post;
    }

    protected ChangeAvatarResponse(Parcel in) {
        avatar = in.readString();
        post = in.readParcelable(Post.class.getClassLoader());
    }

    public static final Creator<ChangeAvatarResponse> CREATOR = new Creator<ChangeAvatarResponse>() {
        @Override
        public ChangeAvatarResponse createFromParcel(Parcel in) {
            return new ChangeAvatarResponse(in);
        }

        @Override
        public ChangeAvatarResponse[] newArray(int size) {
            return new ChangeAvatarResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatar);
        dest.writeParcelable(post, flags);
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
