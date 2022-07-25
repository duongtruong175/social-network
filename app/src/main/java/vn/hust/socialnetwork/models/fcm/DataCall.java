package vn.hust.socialnetwork.models.fcm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataCall {
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("user_avatar")
    @Expose
    private String userAvatar;
    @SerializedName("sender_fcm_token")
    @Expose
    private String senderFcmToken;
    @SerializedName("meeting_type")
    @Expose
    private String meetingType;
    @SerializedName("meeting_room_id")
    @Expose
    private String meetingRoomId;

    public DataCall(String type, String userName, String userAvatar, String senderFcmToken, String meetingType, String meetingRoomId) {
        this.type = type;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.senderFcmToken = senderFcmToken;
        this.meetingType = meetingType;
        this.meetingRoomId = meetingRoomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getSenderFcmToken() {
        return senderFcmToken;
    }

    public void setSenderFcmToken(String senderFcmToken) {
        this.senderFcmToken = senderFcmToken;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public String getMeetingRoomId() {
        return meetingRoomId;
    }

    public void setMeetingRoomId(String meetingRoomId) {
        this.meetingRoomId = meetingRoomId;
    }
}
