package vn.hust.socialnetwork.models.post;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReactUser {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("user")
    @Expose
    private UserPost user;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public ReactUser(int id, UserPost user, int type, String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
