package com.conlistech.sportsclubbookingengine.models;

import java.util.ArrayList;

public class BaseMessage {

    String message;
    String User;
    ArrayList<UserMessage> mArrMessageList;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public ArrayList<UserMessage> getmArrMessageList() {
        return mArrMessageList;
    }

    public void setmArrMessageList(ArrayList<UserMessage> mArrMessageList) {
        this.mArrMessageList = mArrMessageList;
    }
}
