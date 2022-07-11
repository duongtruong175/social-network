package vn.hust.socialnetwork.models.authentication;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {
    @SerializedName("user_id")
    @Expose
    private int userId;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("confirm_password")
    @Expose
    private String confirmPassword;

    public ChangePasswordRequest(int userId, String password, String confirmPassword) {
        this.userId = userId;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
