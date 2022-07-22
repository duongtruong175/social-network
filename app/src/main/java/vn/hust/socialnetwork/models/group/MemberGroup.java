package vn.hust.socialnetwork.models.group;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MemberGroup implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("group_id")
    @Expose
    private int groupId;
    @SerializedName("user")
    @Expose
    private UserGroup user;
    @SerializedName("member_group_status_code")
    @Expose
    private int memberGroupStatusCode;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public MemberGroup(int id, int groupId, UserGroup user, int memberGroupStatusCode, String createdAt, String updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.user = user;
        this.memberGroupStatusCode = memberGroupStatusCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected MemberGroup(Parcel in) {
        id = in.readInt();
        groupId = in.readInt();
        user = in.readParcelable(UserGroup.class.getClassLoader());
        memberGroupStatusCode = in.readInt();
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    public static final Creator<MemberGroup> CREATOR = new Creator<MemberGroup>() {
        @Override
        public MemberGroup createFromParcel(Parcel in) {
            return new MemberGroup(in);
        }

        @Override
        public MemberGroup[] newArray(int size) {
            return new MemberGroup[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(groupId);
        dest.writeParcelable(user, flags);
        dest.writeInt(memberGroupStatusCode);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public UserGroup getUser() {
        return user;
    }

    public void setUser(UserGroup user) {
        this.user = user;
    }

    public int getMemberGroupStatusCode() {
        return memberGroupStatusCode;
    }

    public void setMemberGroupStatusCode(int memberGroupStatusCode) {
        this.memberGroupStatusCode = memberGroupStatusCode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
