package vn.hust.socialnetwork.models.story;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserStory implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("avatar")
    @Expose
    private String avatar;

    public UserStory(int id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    protected UserStory(Parcel in) {
        id = in.readInt();
        name = in.readString();
        avatar = in.readString();
    }

    public static final Creator<UserStory> CREATOR = new Creator<UserStory>() {
        @Override
        public UserStory createFromParcel(Parcel in) {
            return new UserStory(in);
        }

        @Override
        public UserStory[] newArray(int size) {
            return new UserStory[size];
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
        parcel.writeString(avatar);
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
