package vn.hust.socialnetwork.models.fcm;

public class DataMessageSender {
    private Object data;
    private String to; // if wants send many device -> registration_ids: []

    public DataMessageSender(Object data, String to) {
        this.data = data;
        this.to = to;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
