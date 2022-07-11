package vn.hust.socialnetwork.models.userprofile;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import vn.hust.socialnetwork.models.post.Post;

public class ChangeCoverImageResponse implements Parcelable {
    @SerializedName("cover_image")
    @Expose
    private String coverImage;
    @SerializedName("post")
    @Expose
    private Post post;

    public ChangeCoverImageResponse(String coverImage, Post post) {
        this.coverImage = coverImage;
        this.post = post;
    }

    protected ChangeCoverImageResponse(Parcel in) {
        coverImage = in.readString();
        post = in.readParcelable(Post.class.getClassLoader());
    }

    public static final Creator<ChangeCoverImageResponse> CREATOR = new Creator<ChangeCoverImageResponse>() {
        @Override
        public ChangeCoverImageResponse createFromParcel(Parcel in) {
            return new ChangeCoverImageResponse(in);
        }

        @Override
        public ChangeCoverImageResponse[] newArray(int size) {
            return new ChangeCoverImageResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(coverImage);
        dest.writeParcelable(post, flags);
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
