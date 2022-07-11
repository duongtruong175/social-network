package vn.hust.socialnetwork.models.post;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CountsPost implements Parcelable {
    @SerializedName("react_count")
    @Expose
    private int reactCount;
    @SerializedName("comment_count")
    @Expose
    private int commentCount;

    public CountsPost(int reactCount, int commentCount) {
        this.reactCount = reactCount;
        this.commentCount = commentCount;
    }

    protected CountsPost(Parcel in) {
        reactCount = in.readInt();
        commentCount = in.readInt();
    }

    public static final Creator<CountsPost> CREATOR = new Creator<CountsPost>() {
        @Override
        public CountsPost createFromParcel(Parcel in) {
            return new CountsPost(in);
        }

        @Override
        public CountsPost[] newArray(int size) {
            return new CountsPost[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(reactCount);
        parcel.writeInt(commentCount);
    }

    public int getReactCount() {
        return reactCount;
    }

    public void setReactCount(int reactCount) {
        this.reactCount = reactCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
