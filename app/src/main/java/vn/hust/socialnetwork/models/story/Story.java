package vn.hust.socialnetwork.models.story;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import vn.hust.socialnetwork.models.media.Media;

public class Story implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("user")
    @Expose
    private UserStory user;
    @SerializedName("media")
    @Expose
    private Media media;
    @SerializedName("end_time")
    @Expose
    private String endTime;
    @SerializedName("viewed")
    @Expose
    private boolean viewed;
    @SerializedName("viewer_count")
    @Expose
    private int viewerCount;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Story(int id, UserStory user, Media media, String endTime, boolean viewed, int viewerCount, String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.media = media;
        this.endTime = endTime;
        this.viewed = viewed;
        this.viewerCount = viewerCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected Story(Parcel in) {
        id = in.readInt();
        user = in.readParcelable(UserStory.class.getClassLoader());
        media = in.readParcelable(Media.class.getClassLoader());
        endTime = in.readString();
        viewed = in.readByte() != 0;
        viewerCount = in.readInt();
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    public static final Creator<Story> CREATOR = new Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
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
        dest.writeParcelable(media, flags);
        dest.writeString(endTime);
        dest.writeByte((byte) (viewed ? 1 : 0));
        dest.writeInt(viewerCount);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserStory getUser() {
        return user;
    }

    public void setUser(UserStory user) {
        this.user = user;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    public void setViewerCount(int viewerCount) {
        this.viewerCount = viewerCount;
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
