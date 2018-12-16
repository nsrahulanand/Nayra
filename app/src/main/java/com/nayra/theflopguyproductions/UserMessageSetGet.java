package com.nayra.theflopguyproductions;

public class UserMessageSetGet {

    private String Message, Type, User;
    private long  Time;
    private boolean Seen;

    public UserMessageSetGet(String Message, String Type, String User, long Time, boolean Seen) {
        this.Message = Message;
        this.Type = Type;
        this.User = User;
        this.Time = Time;
        this.Seen = Seen;
    }

    public UserMessageSetGet() {
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String Message) {
        this.Message = Message;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String User) {
        this.User = User;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long Time) {
        this.Time = Time;
    }

    public boolean isSeen() {
        return Seen;
    }

    public void setSeen(boolean Seen) {
        this.Seen = Seen;
    }
}
