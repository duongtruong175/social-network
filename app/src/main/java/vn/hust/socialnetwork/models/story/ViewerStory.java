package vn.hust.socialnetwork.models.story;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ViewerStory {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("user")
    @Expose
    private UserStory user;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public ViewerStory(int id, UserStory user, String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
