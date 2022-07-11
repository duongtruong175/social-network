package vn.hust.socialnetwork.models.media;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Media implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("domain")
    @Expose
    private String domain;
    @SerializedName("path")
    @Expose
    private String path;
    @SerializedName("src")
    @Expose
    private String src;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Media(int id, String type, String name, String domain, String path, String src, String createdAt, String updatedAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.domain = domain;
        this.path = path;
        this.src = src;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected Media(Parcel in) {
        id = in.readInt();
        type = in.readString();
        name = in.readString();
        domain = in.readString();
        path = in.readString();
        src = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(type);
        parcel.writeString(name);
        parcel.writeString(domain);
        parcel.writeString(path);
        parcel.writeString(src);
        parcel.writeString(createdAt);
        parcel.writeString(updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
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
