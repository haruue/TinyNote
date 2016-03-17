package cn.com.caoyue.tinynote;

public class MessageItem {

    private int id, sign;
    private String message, time, hash;

    public MessageItem(int id, String time, int sign, String message, String hash) {
        this.id = id;
        this.message = message;
        this.time = time;
        this.sign = sign;
        this.hash = hash;
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

    public String getHash() {
        return hash;
    }
}
