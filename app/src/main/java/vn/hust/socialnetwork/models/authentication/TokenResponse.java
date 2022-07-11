package vn.hust.socialnetwork.models.authentication;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import vn.hust.socialnetwork.models.user.User;

public class TokenResponse {
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("user")
    @Expose
    private User user;

    public TokenResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
