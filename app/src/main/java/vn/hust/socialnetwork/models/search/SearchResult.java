package vn.hust.socialnetwork.models.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.hust.socialnetwork.models.group.Group;
import vn.hust.socialnetwork.models.post.Post;
import vn.hust.socialnetwork.models.user.User;

public class SearchResult {
    @SerializedName("users")
    @Expose
    private List<User> users;
    @SerializedName("groups")
    @Expose
    private List<Group> groups;
    @SerializedName("posts")
    @Expose
    private List<Post> posts;

    public SearchResult(List<User> users, List<Group> groups, List<Post> posts) {
        this.users = users;
        this.groups = groups;
        this.posts = posts;
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

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
