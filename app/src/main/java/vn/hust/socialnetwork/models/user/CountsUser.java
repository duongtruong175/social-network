package vn.hust.socialnetwork.models.user;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CountsUser implements Parcelable {
    @SerializedName("follow_count")
    @Expose
    private int followCount;
    @SerializedName("react_count")
    @Expose
    private int reactCount;
    @SerializedName("post_count")
    @Expose
    private int postCount;
    @SerializedName("friend_count")
    @Expose
    private int friendCount;

    public CountsUser(int followCount, int reactCount, int postCount, int friendCount) {
        this.followCount = followCount;
        this.reactCount = reactCount;
        this.postCount = postCount;
        this.friendCount = friendCount;
    }

    protected CountsUser(Parcel in) {
        followCount = in.readInt();
        reactCount = in.readInt();
        postCount = in.readInt();
        friendCount = in.readInt();
    }

    public static final Creator<CountsUser> CREATOR = new Creator<CountsUser>() {
        @Override
        public CountsUser createFromParcel(Parcel in) {
            return new CountsUser(in);
        }

        @Override
        public CountsUser[] newArray(int size) {
            return new CountsUser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(followCount);
        parcel.writeInt(reactCount);
        parcel.writeInt(postCount);
        parcel.writeInt(friendCount);
    }

    public int getFollowCount() {
        return followCount;
    }

    public void setFollowCount(int followCount) {
        this.followCount = followCount;
    }

    public int getReactCount() {
        return reactCount;
    }

    public void setReactCount(int reactCount) {
        this.reactCount = reactCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getFriendCount() {
        return friendCount;
    }

    public void setFriendCount(int friendCount) {
        this.friendCount = friendCount;
    }
}
