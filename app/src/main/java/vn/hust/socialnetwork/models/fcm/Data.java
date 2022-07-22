package vn.hust.socialnetwork.models.fcm;

public class Data {
    private String title;
    private String body;
    private String type;
    private String url;

    public Data(String title, String body, String type, String url) {
        this.title = title;
        this.body = body;
        this.type = type;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
