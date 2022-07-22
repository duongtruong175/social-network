package vn.hust.socialnetwork.models.group;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("admin")
    @Expose
    private UserGroup admin;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("cover_image")
    @Expose
    private String coverImage;
    @SerializedName("introduction")
    @Expose
    private String introduction;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("preview_member")
    @Expose
    private UserGroup previewMember;
    @SerializedName("counts")
    @Expose
    private CountsGroup counts;
    @SerializedName("current_user_status")
    @Expose
    private String currentUserStatus;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Group(int id, UserGroup admin, String name, String coverImage, String introduction, int type, UserGroup previewMember, CountsGroup counts, String currentUserStatus, String createdAt, String updatedAt) {
        this.id = id;
        this.admin = admin;
        this.name = name;
        this.coverImage = coverImage;
        this.introduction = introduction;
        this.type = type;
        this.previewMember = previewMember;
        this.counts = counts;
        this.currentUserStatus = currentUserStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected Group(Parcel in) {
        id = in.readInt();
        admin = in.readParcelable(UserGroup.class.getClassLoader());
        name = in.readString();
        coverImage = in.readString();
        introduction = in.readString();
        type = in.readInt();
        previewMember = in.readParcelable(UserGroup.class.getClassLoader());
        counts = in.readParcelable(CountsGroup.class.getClassLoader());
        currentUserStatus = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(admin, flags);
        dest.writeString(name);
        dest.writeString(coverImage);
        dest.writeString(introduction);
        dest.writeInt(type);
        dest.writeParcelable(previewMember, flags);
        dest.writeParcelable(counts, flags);
        dest.writeString(currentUserStatus);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserGroup getAdmin() {
        return admin;
    }

    public void setAdmin(UserGroup admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UserGroup getPreviewMember() {
        return previewMember;
    }

    public void setPreviewMember(UserGroup previewMember) {
        this.previewMember = previewMember;
    }

    public CountsGroup getCounts() {
        return counts;
    }

    public void setCounts(CountsGroup counts) {
        this.counts = counts;
    }

    public String getCurrentUserStatus() {
        return currentUserStatus;
    }

    public void setCurrentUserStatus(String currentUserStatus) {
        this.currentUserStatus = currentUserStatus;
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
