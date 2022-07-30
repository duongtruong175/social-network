package vn.hust.socialnetwork.models.post;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import vn.hust.socialnetwork.models.media.Media;

public class CommentPost implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("user")
    @Expose
    private UserPost user;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("media")
    @Expose
    private Media media;
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

    public CommentPost(int id, UserPost user, String content, Media media, int reactStatus, ReactCount reactCount, CountsPost counts, String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.media = media;
        this.reactStatus = reactStatus;
        this.reactCount = reactCount;
        this.counts = counts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected CommentPost(Parcel in) {
        id = in.readInt();
        user = in.readParcelable(UserPost.class.getClassLoader());
        content = in.readString();
        media = in.readParcelable(Media.class.getClassLoader());
        reactStatus = in.readInt();
        reactCount = in.readParcelable(ReactCount.class.getClassLoader());
        counts = in.readParcelable(CountsPost.class.getClassLoader());
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    public static final Creator<CommentPost> CREATOR = new Creator<CommentPost>() {
        @Override
        public CommentPost createFromParcel(Parcel in) {
            return new CommentPost(in);
        }

        @Override
        public CommentPost[] newArray(int size) {
            return new CommentPost[size];
        }
    };

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(user, flags);
        dest.writeString(content);
        dest.writeParcelable(media, flags);
        dest.writeInt(reactStatus);
        dest.writeParcelable(reactCount, flags);
        dest.writeParcelable(counts, flags);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }
}
