package vn.hust.socialnetwork.models.notification;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("receiver_id")
    @Expose
    private int receiverId;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("notification_status_code")
    @Expose
    private int notificationStatusCode;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Notification(int id, int receiverId, String imageUrl, int type, String content, String url, int notificationStatusCode, String createdAt, String updatedAt) {
        this.id = id;
        this.receiverId = receiverId;
        this.imageUrl = imageUrl;
        this.type = type;
        this.content = content;
        this.url = url;
        this.notificationStatusCode = notificationStatusCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNotificationStatusCode() {
        return notificationStatusCode;
    }

    public void setNotificationStatusCode(int notificationStatusCode) {
        this.notificationStatusCode = notificationStatusCode;
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
