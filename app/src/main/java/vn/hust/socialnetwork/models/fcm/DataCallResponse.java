package vn.hust.socialnetwork.models.fcm;

public class DataCallResponse {
    private String type;
    private String answer;

    public DataCallResponse(String type, String answer) {
        this.type = type;
        this.answer = answer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
