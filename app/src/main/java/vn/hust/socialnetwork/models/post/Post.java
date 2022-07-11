package vn.hust.socialnetwork.models.post;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import vn.hust.socialnetwork.models.media.Media;

public class Post implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("user")
    @Expose
    private UserPost user;
    @SerializedName("group")
    @Expose
    private GroupPost group;
    @SerializedName("caption")
    @Expose
    private String caption;
    @SerializedName("media")
    @Expose
    private Media media;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("react_status")
    @Expose
    private int reactStatus;
    @SerializedName("react_count")
    @Expose
    private ReactCount reactCount;
    @SerializedName("counts")
    @Expose
    private CountsPost counts;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Post(int id, UserPost user, GroupPost group, String caption, Media media, int type, int reactStatus, ReactCount reactCount, CountsPost counts, String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.group = group;
        this.caption = caption;
        this.media = media;
        this.type = type;
        this.reactStatus = reactStatus;
        this.reactCount = reactCount;
        this.counts = counts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    protected Post(Parcel in) {
        id = in.readInt();
        user = in.readParcelable(UserPost.class.getClassLoader());
        group = in.readParcelable(GroupPost.class.getClassLoader());
        caption = in.readString();
        media = in.readParcelable(Media.class.getClassLoader());
        type = in.readInt();
        reactStatus = in.readInt();
        reactCount = in.readParcelable(ReactCount.class.getClassLoader());
        counts = in.readParcelable(CountsPost.class.getClassLoader());
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(group, flags);
        dest.writeString(caption);
        dest.writeParcelable(media, flags);
        dest.writeInt(type);
        dest.writeInt(reactStatus);
        dest.writeParcelable(reactCount, flags);
        dest.writeParcelable(counts, flags);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserPost getUser() {
        return user;
    }

    public void setUser(UserPost user) {
        this.user = user;
    }

    public GroupPost getGroup() {
        return group;
    }

    public void setGroup(GroupPost group) {
        this.group = group;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getReactStatus() {
        return reactStatus;
    }

    public void setReactStatus(int reactStatus) {
        this.reactStatus = reactStatus;
    }

    public ReactCount getReactCount() {
        return reactCount;
    }

    public void setReactCount(ReactCount reactCount) {
        this.reactCount = reactCount;
    }

    public CountsPost getCounts() {
        return counts;
    }

    public void setCounts(CountsPost counts) {
        this.counts = counts;
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
