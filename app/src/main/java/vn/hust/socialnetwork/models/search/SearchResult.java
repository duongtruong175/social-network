package vn.hust.socialnetwork.models.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.user.User;

public class SearchResult {
    @SerializedName("users")
    @Expose
    private List<User> users;
    @SerializedName("groups")
    @Expose
    private List<Group> groups;

    public SearchResult(List<User> users, List<Group> groups) {
        this.users = users;
        this.groups = groups;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
