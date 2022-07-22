package vn.hust.socialnetwork.models.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Conversation implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("receiver")
    @Expose
    private UserConversation receiver;
    @SerializedName("room_chat_id")
    @Expose
    private String roomChatId;
    @SerializedName("is_friend")
    @Expose
    private boolean isFriend;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Conversation(int id, UserConversation receiver, String roomChatId, boolean isFriend, String createdAt, String updatedAt) {
        this.id = id;
        this.receiver = receiver;
        this.roomChatId = roomChatId;
        this.isFriend = isFriend;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected Conversation(Parcel in) {
        id = in.readInt();
        receiver = in.readParcelable(UserConversation.class.getClassLoader());
        roomChatId = in.readString();
        isFriend = in.readByte() != 0;
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(receiver, flags);
        dest.writeString(roomChatId);
        dest.writeByte((byte) (isFriend ? 1 : 0));
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserConversation getReceiver() {
        return receiver;
    }

    public void setReceiver(UserConversation receiver) {
        this.receiver = receiver;
    }

    public String getRoomChatId() {
        return roomChatId;
    }

    public void setRoomChatId(String roomChatId) {
        this.roomChatId = roomChatId;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
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
