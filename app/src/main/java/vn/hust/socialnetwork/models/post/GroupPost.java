package vn.hust.socialnetwork.models.post;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupPost implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;

    public GroupPost(int id, String name) {
        this.id = id;
        this.name = name;
    }

    protected GroupPost(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    public static final Creator<GroupPost> CREATOR = new Creator<GroupPost>() {
        @Override
        public GroupPost createFromParcel(Parcel in) {
            return new GroupPost(in);
        }

        @Override
        public GroupPost[] newArray(int size) {
            return new GroupPost[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
