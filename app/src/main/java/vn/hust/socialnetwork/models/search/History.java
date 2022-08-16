package vn.hust.socialnetwork.models.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class History {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("query")
    @Expose
    private String query;
    @SerializedName("count")
    @Expose
    private int count;

    public History(int id, String query, int count) {
        this.id = id;
        this.query = query;
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
