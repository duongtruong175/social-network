package vn.hust.socialnetwork.models.relation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RelationUser {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("user")
    @Expose
    private DestinationUser user;
    @SerializedName("relation")
    @Expose
    private String relation;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public RelationUser(int id, DestinationUser user, String relation, String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.relation = relation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DestinationUser getUser() {
        return user;
    }

    public void setUser(DestinationUser user) {
        this.user = user;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
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
