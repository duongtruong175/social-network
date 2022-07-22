package vn.hust.socialnetwork.models.chat;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Message {
    private String id;
    private int senderId;
    private int receiverId;
    private String content;
    private ReplyToMessage replyToMessage;
    private int reactType;
    private int messageStatusCode;
    private int deleted;
    private long createdAt;
    private long updatedAt;

    // Default constructor required for calls to DataSnapshot.getValue
    public Message() {
    }

    public Message(String id, int senderId, int receiverId, String content, ReplyToMessage replyToMessage, int reactType, int messageStatusCode, int deleted, long createdAt, long updatedAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.replyToMessage = replyToMessage;
        this.reactType = reactType;
        this.messageStatusCode = messageStatusCode;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ReplyToMessage getReplyToMessage() {
        return replyToMessage;
    }

    public void setReplyToMessage(ReplyToMessage replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    public int getReactType() {
        return reactType;
    }

    public void setReactType(int reactType) {
        this.reactType = reactType;
    }

    public int getMessageStatusCode() {
        return messageStatusCode;
    }

    public void setMessageStatusCode(int messageStatusCode) {
        this.messageStatusCode = messageStatusCode;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("senderId", senderId);
        result.put("receiverId", receiverId);
        result.put("content", content);
        result.put("replyToMessage", replyToMessage.toMap());
        result.put("reactType", reactType);
        result.put("messageStatusCode", messageStatusCode);
        result.put("deleted", deleted);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }
}
