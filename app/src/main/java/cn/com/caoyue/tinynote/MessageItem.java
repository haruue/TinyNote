package cn.com.caoyue.tinynote;

public class MessageItem {

    private int id, sign;
    private String message, time;

    public MessageItem(int id, String time, int sign, String message) {
        this.id = id;
        this.message = message;
        this.time = time;
        this.sign = sign;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public int getSign() {
        return sign;
    }
}
