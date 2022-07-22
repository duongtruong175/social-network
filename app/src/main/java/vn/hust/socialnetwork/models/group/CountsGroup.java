package vn.hust.socialnetwork.models.group;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CountsGroup implements Parcelable {
    @SerializedName("member_count")
    @Expose
    private int memberCount;
    @SerializedName("post_count")
    @Expose
    private int postCount;

    public CountsGroup(int memberCount, int postCount) {
        this.memberCount = memberCount;
        this.postCount = postCount;
    }

    protected CountsGroup(Parcel in) {
        memberCount = in.readInt();
        postCount = in.readInt();
    }

    public static final Creator<CountsGroup> CREATOR = new Creator<CountsGroup>() {
        @Override
        public CountsGroup createFromParcel(Parcel in) {
            return new CountsGroup(in);
        }

        @Override
        public CountsGroup[] newArray(int size) {
            return new CountsGroup[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(memberCount);
        dest.writeInt(postCount);
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }
}
