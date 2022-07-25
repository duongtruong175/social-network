package vn.hust.socialnetwork.models.fcm;

public class FCMResponse {
    private int success;

    public FCMResponse(int success) {
        this.success = success;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }
}
