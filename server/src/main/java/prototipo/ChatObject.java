package prototipo;

import java.time.Instant;

public class ChatObject {

    private String userName;
    private String from;
    private String message;
    private String to;
    private int count;
    private Instant timeStamp;

    public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public ChatObject() {
    }

    
    public ChatObject(String from, String to, String body, int count, Instant timeStamp) {
        super();
        this.from = from;
        this.to = to;
        this.message = body;
        this.count = count;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }


	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Instant getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Instant timeStamp) {
		this.timeStamp = timeStamp;
	}

}
