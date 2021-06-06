package sample;

import java.util.Date;

/*
 * Message received from server.
 *
 * @Author Jay Sridhar
 */
public class ServerMessage {
    private String sender;
    private String message;
    private String topic;
    private final Date time = new Date();

    public ServerMessage() {
    }

    public ServerMessage(String sender, String message, String topic) {
        this.sender = sender;
        this.message = message;
        this.topic = topic;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Date getTime() {
        return time;
    }

    public String toString() {
        return String
                .format("%2$s|%1$-10s: %3$s",
                        getSender(), getTime().toString(),
                        getMessage());
    }
}
